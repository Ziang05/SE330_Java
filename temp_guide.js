const fs = require('fs');

const rxPath = 'C:\\Users\\ASUS\\Downloads\\hospital-frontend\\src\\pages\\reception\\ReceptionPage.jsx';
let rx = fs.readFileSync(rxPath, 'utf8').replace(/\r\n/g, '\n');

// 1. Update the "Next Steps" description to be clearer
rx = rx.replace(
  'After check-in, create an invoice or forward to the prescription screen.',
  'After check-in, create the invoice first (examination fee only). Then add prescriptions & lab tests — the invoice updates automatically!'
);

// 2. Add a "Refresh Invoice" button next to the invoice summary
// Find the section where the invoice is displayed and add a refresh button
const invoiceHeader = `              <div className="flex items-center justify-between">
                <h3 className="text-lg font-semibold text-slate-900">
                  Invoice #{createdInvoice.id}
                </h3>`;

const invoiceHeaderWithRefresh = `              <div className="flex items-center justify-between">
                <h3 className="text-lg font-semibold text-slate-900">
                  Invoice #{createdInvoice.id}
                </h3>
                {createdInvoice.status === 'PENDING' && (
                  <button
                    type="button"
                    onClick={handleRecalculate}
                    disabled={isRecalculating}
                    className="inline-flex items-center gap-1.5 rounded-xl bg-amber-100 px-3 py-1.5 text-xs font-semibold text-amber-700 hover:bg-amber-200 disabled:opacity-50"
                  >
                    <RefreshCw className="h-3 w-3" />
                    {isRecalculating ? 'Updating...' : 'Refresh'}
                  </button>
                )}`;

if (rx.includes(invoiceHeader)) {
  rx = rx.replace(invoiceHeader, invoiceHeaderWithRefresh);
  console.log('OK: Refresh button added to invoice header');
} else {
  console.log('WARN: Could not find invoice header');
}

// 3. Add a step-by-step guide card below the Next Steps section
const nextStepsEnd = rx.indexOf('</div>\n            </div>\n\n            {checkedInRecord &&');
if (nextStepsEnd > -1) {
  // Check if guide already exists
  if (!rx.includes('Workflow Guide')) {
    const guideCard = `
            {/* Workflow Guide */}
            {checkedInRecord && (
              <div className="rounded-3xl border border-blue-200 bg-blue-50 p-5 shadow-sm">
                <h3 className="text-base font-semibold text-blue-900">Workflow Guide</h3>
                <div className="mt-3 space-y-2 text-sm text-blue-800">
                  <div className="flex items-start gap-2">
                    <span className={\`flex h-5 w-5 shrink-0 items-center justify-center rounded-full text-xs font-bold \${checkedInRecord ? 'bg-green-500 text-white' : 'bg-blue-200 text-blue-600'}\`}>1</span>
                    <span>Check in patient {checkedInRecord ? '✓' : ''}</span>
                  </div>
                  <div className="flex items-start gap-2">
                    <span className={\`flex h-5 w-5 shrink-0 items-center justify-center rounded-full text-xs font-bold \${createdInvoice ? 'bg-green-500 text-white' : 'bg-blue-200 text-blue-600'}\`}>2</span>
                    <span>Create invoice {createdInvoice ? '✓' : '(click "Create Invoice" above)'}</span>
                  </div>
                  <div className="flex items-start gap-2">
                    <span className={\`flex h-5 w-5 shrink-0 items-center justify-center rounded-full text-xs font-bold bg-blue-200 text-blue-600\`}>3</span>
                    <span>Add prescriptions & lab tests (click "Go to prescription" above)</span>
                  </div>
                  <div className="flex items-start gap-2">
                    <span className={\`flex h-5 w-5 shrink-0 items-center justify-center rounded-full text-xs font-bold bg-blue-200 text-blue-600\`}>4</span>
                    <span>Invoice auto-updates! Or click "Refresh" on the invoice to reload fees</span>
                  </div>
                  <div className="flex items-start gap-2">
                    <span className={\`flex h-5 w-5 shrink-0 items-center justify-center rounded-full text-xs font-bold bg-blue-200 text-blue-600\`}>5</span>
                    <span>Go to invoice detail page to process payment</span>
                  </div>
                </div>
              </div>
            )}
`;
    // Insert after the "Next Steps" section closing div
    const insertPoint = rx.indexOf('\n            {checkedInRecord && (\n              <div className="rounded-3xl border border-emerald-200');
    if (insertPoint > -1) {
      rx = rx.substring(0, insertPoint) + guideCard + rx.substring(insertPoint);
      console.log('OK: Workflow Guide card added');
    } else {
      console.log('WARN: Could not find insertion point for guide');
    }
  }
}

// 4. Ensure RefreshCw is imported (from previous changes)
if (!rx.includes('RefreshCw')) {
  rx = rx.replace(
    /import \{([^}]+)\} from 'lucide-react';/,
    (match, icons) => {
      if (icons.includes('RefreshCw')) return match;
      return `import {${icons}, RefreshCw} from 'lucide-react';`;
    }
  );
  console.log('OK: RefreshCw added to imports');
}

fs.writeFileSync(rxPath, rx, 'utf8');

// Verify
const v = fs.readFileSync(rxPath, 'utf8');
console.log('\nVerification:');
console.log(`  ${v.includes('Refresh') ? 'OK' : 'FAIL'}: Refresh button`);
console.log(`  ${v.includes('Workflow Guide') ? 'OK' : 'FAIL'}: Workflow Guide`);
console.log(`  ${v.includes('auto-updates') ? 'OK' : 'FAIL'}: Auto-update hint`);
