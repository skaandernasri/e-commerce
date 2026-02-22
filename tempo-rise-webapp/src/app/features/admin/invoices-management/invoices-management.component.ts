import {
  ChangeDetectorRef,
  Component,
  OnInit,
  signal,
  computed,
  WritableSignal,
  effect
} from '@angular/core';
import { InvoiceResponse } from '../../../core/models/invoice';
import { InvoiceService } from '../../../core/services/invoice.service';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmDialogComponent } from '../../../shared/components/dialogs/confirm-dialog/confirm-dialog.component';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';
import { ExpandLongTextComponent } from '../../../shared/components/expand-long-text/expand-long-text.component';
import { CurrencyFormatPipe } from '../../../core/pipe/currency-pipe.pipe';
import { Router } from '@angular/router';
import { ORDER_STATUS_LIST, ORDER_STATUS_RECORDS, ORDER_STATUS_TYPES } from '../../../core/models/order';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-invoices-management',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    PaginationComponent,
    CurrencyFormatPipe,
    TranslatePipe
  ],
  templateUrl: './invoices-management.component.html',
  styleUrl: './invoices-management.component.css'
})
export class InvoicesManagementComponent implements OnInit {
  invoices = signal<InvoiceResponse[]>([]);
  searchTerm = signal('');
  sortByDate = signal<'asc' | 'desc' | ''>('');
  sortByTotal = signal<'asc' | 'desc' | ''>('');
  selectedStatus: WritableSignal <ORDER_STATUS_TYPES> = signal('ALL');
  //status = signal<ORDER_STATUS_TYPES>('ALL');
  ORDER_STATUS_RECORDS = ORDER_STATUS_RECORDS;
  currentPage = signal(1);
  itemsPerPage = 10;
  isLoading = true;
  errorMessage = '';

  // Metrics
  currentWeekInvoices = 0;
  lastWeekInvoices = 0;
  invoicesChangePercentage = 0;
  currentWeekInvoicesTotalAmount = 0;
  lastWeekInvoicesTotalAmount = 0;
  invoicesTotalAmountChangePercentage = 0;

  constructor(private invoiceService: InvoiceService, private dialog: MatDialog,
      private router: Router
  ) {
    effect(() => {
      const paginatedInvoices = this.paginatedInvoices();      
      this.calculateMetrics(); // reacts to filteredInvoices
    });
  }

  ngOnInit(): void {
    this.loadInvoices();
  }


  
  filteredInvoices = computed(() => {
    const term = this.searchTerm().toLowerCase();
    const status = this.selectedStatus();
    let data = this.invoices();    
    return data.filter(invoice => {
      const matchesStatus = status === 'ALL' || invoice.commande.statut === status;
      const matchesSearch = !term || invoice.commande.email.toLowerCase().includes(term);
      return matchesStatus && matchesSearch;
    });
  });
  inProgressInvoices = computed(() => {
    return this.filteredInvoices().filter(invoice => invoice.commande.statut === 'EN_COURS');
  })
  shippedInvoices = computed(() => {
    return this.filteredInvoices().filter(invoice => invoice.commande.statut === 'EXPEDIEE');
  })
  deliveredInvoices = computed(() => {
    return this.filteredInvoices().filter(invoice => invoice.commande.statut === 'LIVREE');
  })
  sortedInvoices = computed(() => {
    const sortDate = this.sortByDate();
    const sortTotal = this.sortByTotal();
    let data = [...this.filteredInvoices()];

    if (sortDate) {
      data.sort((a, b) => {
        const d1 = new Date(a.dateEmission).getTime();
        const d2 = new Date(b.dateEmission).getTime();
        return sortDate === 'asc' ? d1 - d2 : d2 - d1;
      });
    }

    if (sortTotal) {
      data.sort((a, b) => sortTotal === 'asc' ? a.total - b.total : b.total - a.total);
    }

    return data;
  });
  paginatedInvoices = computed(() => {
    const start = (this.currentPage() - 1) * this.itemsPerPage;
    return this.sortedInvoices().slice(start, start + this.itemsPerPage);
  });

  onPageChange(newPage: number): void {
    this.currentPage.set(newPage);
  }
  filterByStatus():void{
    this.sortByDate.set('');
    this.sortByTotal.set(''); 
    //this.status.set(this.selectedStatus);
  }
  sortInvoicesByDate(): void {
    this.sortByTotal.set('');
    const current = this.sortByDate();
    this.sortByDate.set(current === 'asc' ? 'desc' : current === 'desc' ? 'asc' : 'desc');
  }

  sortInvoicesByTotal(): void {
    this.sortByDate.set('');
    const current = this.sortByTotal();
    this.sortByTotal.set(current === 'asc' ? 'desc' : current === 'desc' ? 'asc' : 'desc');
  }
  private calculateMetrics(): void {
    const currentWeek = this.getInvoicesForPeriod(0, 7);
    const lastWeek = this.getInvoicesForPeriod(7, 14);

    this.currentWeekInvoices = currentWeek.length;
    this.lastWeekInvoices = lastWeek.length;
    this.invoicesChangePercentage = this.calculatePercentageChange(
      this.currentWeekInvoices,
      this.lastWeekInvoices
    );
    this.currentWeekInvoicesTotalAmount=currentWeek.reduce((total, invoice) => total + invoice.total, 0);
    this.lastWeekInvoicesTotalAmount=lastWeek.reduce((total, invoice) => total + invoice.total, 0);
    this.invoicesTotalAmountChangePercentage = this.calculatePercentageChange(
      this.currentWeekInvoicesTotalAmount,
      this.lastWeekInvoicesTotalAmount
    );
  }

  private getInvoicesForPeriod(daysStart: number, daysEnd: number): InvoiceResponse[] {
    const now = new Date();
    const startDate = new Date(now.getTime() - daysStart * 86400000);
    const endDate = new Date(now.getTime() - daysEnd * 86400000);

    return this.filteredInvoices().filter(invoice => {
      const date = new Date(invoice.dateEmission);
      return date <= startDate && date > endDate;
    });
  }

  private calculatePercentageChange(current: number, previous: number): number {
    if (previous === 0) return current > 0 ? 100 : 0;
    return ((current - previous) / previous) * 100;
  }
  calculateTotalInvoicesAmount(): number {
    return this.filteredInvoices().reduce((total, invoice) => total + invoice.total, 0);
  }

  hasIncreased(current: number, last: number): boolean {
    return current > last;
  }

  loadInvoices(): void {
    this.isLoading = true;
    this.invoiceService.getAllInvoices().subscribe({
      next: (data) => {
        this.invoices.set(data);
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = err.message;
        this.isLoading = false;
      }
    });
  }
  deleteInvoice(id: number): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Delete Invoice',
        message: 'Are you sure you want to delete this invoice?'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.invoiceService.deleteInvoice(id).subscribe({
          next: () => {
            this.loadInvoices();
          },
          error: (err) => {
            this.errorMessage = 'Failed to delete invoice';
          }
        });
      }
    });
  }
  viewInvoice(id: number): void {
    this.router.navigate([`/admin/invoices/${id}`]);
  }
}
