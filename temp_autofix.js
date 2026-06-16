const fs = require('fs');

// 1. Add recalculateInvoice to invoicesApi.js (idempotent)
const apiPath = 'C:\\Users\\ASUS\\Downloads\\hospital-frontend\\src\\api\\invoicesApi.js';
let api = fs.readFileSync(apiPath, 'utf8').replace(/\r\n/g, '\n');
if (!api.includes('recalculateInvoice')) {
  api = api.replace(
    'export const createInvoice = (medicalRecordId) => client.post(`/api/v1/invoices/medical-records/${medicalRecordId}`);',
    'export const createInvoice = (medicalRecordId) => client.post(`/api/v1/invoices/medical-records/${medicalRecordId}`);\nexport const recalculateInvoice = (medicalRecordId) => client.put(`/api/v1/invoices/medical-records/${medicalRecordId}/recalculate`);'
  );
  fs.writeFileSync(apiPath, api, 'utf8');
  console.log('OK: recalculateInvoice API added');
} else {
  console.log('SKIP: recalculateInvoice already in API');
}

// 2. Update InvoiceDetailPage - add Recalculate button and auto-refresh
const pagePath = 'C:\\Users\\ASUS\\Downloads\\hospital-frontend\\src\\pages\\invoices\\InvoiceDetailPage.jsx';
let page = fs.readFileSync(pagePath, 'utf8').replace(/\r\n/g, '\n');

// 2a. Add recalculate handler
if (!page.includes('handleRecalculate')) {
  page = page.replace(
    '  const handleExportPdf = async () => {',
    `  const handleRecalculate = async () => {
    if (!invoice?.medicalRecordId) return;
    try {
      await invoicesApi.recalculateInvoice(invoice.medicalRecordId);
      addToast('Invoice recalculated successfully', 'success');
      // Reload invoice
      const response = await invoicesApi.getInvoiceById(id);
      setInvoice(response.data?.data || null);
    } catch (error) {
      addToast(error.response?.data?.message || 'Failed to recalculate invoice', 'error');
    }
  };

  const handleExportPdf = async () => {`
  );
  console.log('OK: handleRecalculate added to InvoiceDetailPage');
}

// 2b. Add Recalculate button next to the Pay Invoice button area
// Find the section right before the Pay Invoice button
const payBtnArea = `          {canPay && (
            <button
              type="button"
              onClick={() => setIsModalOpen(true)}
              className="w-full rounded-2xl bg-sky-600 px-4 py-3 text-sm font-semibold text-white hover:bg-sky-700"
            >
              Pay Invoice
            </button>
          )}`;

const payBtnWithRecalc = `          {invoice?.status === 'PENDING' && (
            <button
              type="button"
              onClick={handleRecalculate}
              className="w-full rounded-2xl bg-amber-600 px-4 py-3 text-sm font-semibold text-white hover:bg-amber-700"
            >
              Recalculate Fees
            </button>
          )}

          {canPay && (
            <button
              type="button"
              onClick={() => setIsModalOpen(true)}
              className="w-full rounded-2xl bg-sky-600 px-4 py-3 text-sm font-semibold text-white hover:bg-sky-700"
            >
              Pay Invoice
            </button>
          )}`;

if (page.includes(payBtnArea)) {
  page = page.replace(payBtnArea, payBtnWithRecalc);
  console.log('OK: Recalculate Fees button added');
} else {
  console.log('WARN: Could not find Pay Invoice button area');
}

// 2c. Add a helpful info box when medicine/lab fees are 0
const billingSection = `          <div className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
            <h3 className="text-lg font-semibold text-slate-900">Billing Breakdown</h3>`;

const billingWithNotice = `          <div className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
            <h3 className="text-lg font-semibold text-slate-900">Billing Breakdown</h3>
            {(invoice?.medicineFee === 0 && invoice?.labFee === 0 && invoice?.status === 'PENDING') && (
              <div className="mt-3 rounded-2xl border border-amber-200 bg-amber-50 p-3">
                <p className="text-sm font-medium text-amber-800">
                  Medicine and Lab fees are 0. Add prescriptions or lab tests, then click "Recalculate Fees" to update.
                </p>
              </div>
            )}`;

if (page.includes(billingSection)) {
  page = page.replace(billingSection, billingWithNotice);
  console.log('OK: Fee notice added to billing section');
} else {
  console.log('WARN: Could not find billing section');
}

fs.writeFileSync(pagePath, page, 'utf8');

// Verify
const v = fs.readFileSync(pagePath, 'utf8');
console.log('\nFinal verification:');
console.log(`  ${v.includes('handleRecalculate') ? 'OK' : 'FAIL'}: Recalculate handler`);
console.log(`  ${v.includes('Recalculate Fees') ? 'OK' : 'FAIL'}: Button text`);
console.log(`  ${v.includes('Medicine and Lab fees are 0') ? 'OK' : 'FAIL'}: Fee notice`);
