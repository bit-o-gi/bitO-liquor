#!/usr/bin/env node
/**
 * liquor_price 의 최신 스냅샷이 liquor_price_history 의 최신 행과 어긋난 경우,
 * 해당 (liquor_id, source) 한 줄을 history 에 추가해 차트 forward-fill 이
 * 카드 최저가와 일치하도록 만든다.
 *
 * 사용법:  node scripts/backfill_liquor_price_history.mjs [--dry-run]
 */
import { readFile } from 'node:fs/promises';
import { dirname, join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const ROOT = resolve(__dirname, '..');

const dryRun = process.argv.includes('--dry-run');

async function loadEnv() {
  const envPath = join(ROOT, 'frontend', '.env.local');
  const text = await readFile(envPath, 'utf8');
  const env = {};
  for (const line of text.split(/\r?\n/)) {
    const m = line.match(/^([A-Z0-9_]+)=(.*)$/);
    if (m) env[m[1]] = m[2].trim();
  }
  if (!env.NEXT_PUBLIC_SUPABASE_URL || !env.SUPABASE_SERVICE_ROLE_KEY) {
    throw new Error('Supabase URL/key missing in frontend/.env.local');
  }
  return {
    url: env.NEXT_PUBLIC_SUPABASE_URL.replace(/\/$/, ''),
    key: env.SUPABASE_SERVICE_ROLE_KEY,
  };
}

async function rest({ url, key }, path, init = {}) {
  const res = await fetch(`${url}/rest/v1/${path}`, {
    ...init,
    headers: {
      apikey: key,
      Authorization: `Bearer ${key}`,
      'Content-Type': 'application/json',
      ...(init.headers ?? {}),
    },
  });
  if (!res.ok) {
    const body = await res.text();
    throw new Error(`${res.status} ${res.statusText} on ${path}: ${body}`);
  }
  if (res.status === 204) return null;
  const text = await res.text();
  if (!text) return null;
  return JSON.parse(text);
}

async function fetchAllLiquorPrices(creds) {
  const rows = [];
  const pageSize = 1000;
  let from = 0;
  for (;;) {
    const page = await rest(creds, `liquor_price?select=liquor_id,source,current_price,original_price,crawled_at&order=liquor_id.asc`, {
      headers: { Range: `${from}-${from + pageSize - 1}` },
    });
    rows.push(...page);
    if (page.length < pageSize) break;
    from += pageSize;
  }
  return rows;
}

async function fetchLatestHistory(creds, liquorId, source) {
  const rows = await rest(
      creds,
      `liquor_price_history?liquor_id=eq.${liquorId}&source=eq.${encodeURIComponent(source)}&select=current_price,original_price,crawled_at&order=crawled_at.desc&limit=1`,
  );
  return rows[0] ?? null;
}

async function insertHistory(creds, payload) {
  return rest(creds, `liquor_price_history`, {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

function priceDiffers(price, history) {
  if (!history) return true;
  return (
      (price.current_price ?? null) !== (history.current_price ?? null) ||
      (price.original_price ?? null) !== (history.original_price ?? null)
  );
}

(async () => {
  const creds = await loadEnv();
  const prices = await fetchAllLiquorPrices(creds);
  console.log(`scanning ${prices.length} liquor_price rows (dry-run=${dryRun})`);

  let inserted = 0;
  let skipped = 0;
  for (const p of prices) {
    if (!p.source) {
      skipped++;
      continue;
    }
    if (p.current_price == null && p.original_price == null) {
      skipped++;
      continue;
    }
    const source = p.source.toUpperCase();
    const latest = await fetchLatestHistory(creds, p.liquor_id, source);
    if (!priceDiffers(p, latest)) {
      skipped++;
      continue;
    }

    // history 의 최신 시각보다 반드시 이후가 되어야 forward-fill 마지막 값으로 잡힘 → now() 사용.
    const crawledAt = new Date().toISOString();
    const payload = {
      liquor_id: p.liquor_id,
      source,
      current_price: p.current_price,
      original_price: p.original_price,
      crawled_at: crawledAt,
    };
    console.log(
        `+ liquor_id=${p.liquor_id} source=${source}  history=${
            latest ? `${latest.current_price}/${latest.original_price}` : '(none)'
        }  ->  price=${p.current_price}/${p.original_price}  @ ${crawledAt}`,
    );
    if (!dryRun) {
      await insertHistory(creds, payload);
    }
    inserted++;
  }

  console.log(`done. ${dryRun ? 'would insert' : 'inserted'} ${inserted}, skipped ${skipped}`);
})().catch((err) => {
  console.error(err);
  process.exit(1);
});