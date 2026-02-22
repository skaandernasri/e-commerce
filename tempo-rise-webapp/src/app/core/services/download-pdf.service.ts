import { Injectable, signal } from '@angular/core';
import html2canvas from 'html2canvas';
import jsPDF from 'jspdf';

@Injectable({
  providedIn: 'root'
})
export class DownloadPdfService {
  downloadingfactureId = signal<number | null>(null);
downloadPDF(container: any, data: any,url: string): void {
  this.downloadingfactureId.set(data?.id);
  const invoice = container.nativeElement;

  html2canvas(invoice, {
    scale: 2, // improves quality
    useCORS: true, // useful if you have external images
    scrollY: -window.scrollY // prevent scroll offset
  }).then(canvas => {
    const imgData = canvas.toDataURL('image/png');
    const pdf = new jsPDF('p', 'mm', 'a4');
    const pdfWidth = pdf.internal.pageSize.getWidth();
    const pdfHeight = (canvas.height * pdfWidth) / canvas.width;

    pdf.addImage(imgData, 'PNG', 0, 0, pdfWidth, pdfHeight);
    pdf.save(`${url}-${data?.id}.pdf`);
  }).catch(error => {
    console.error('PDF generation failed:', error);
  }).finally( ()=>
    this.downloadingfactureId.set(null)
  );
}
  constructor() { }
}
