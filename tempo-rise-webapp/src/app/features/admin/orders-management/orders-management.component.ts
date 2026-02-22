import {
  ChangeDetectorRef,
  Component,
  OnInit,
  signal,
  computed,
  effect,
  inject,
  OnDestroy,
  WritableSignal,
  untracked
} from '@angular/core';
import { OrderService } from '../../../core/services/order.service';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmDialogComponent } from '../../../shared/components/dialogs/confirm-dialog/confirm-dialog.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';
import { DatePipe, CommonModule } from '@angular/common';
import {  ORDER_STATUS_RECORDS, ORDER_STATUS_TYPES, OrderResponse } from '../../../core/models/order';
import { CurrencyFormatPipe } from '../../../core/pipe/currency-pipe.pipe';
import { OrderModalComponent } from './order-modal/order-modal.component';
import { Subject, takeUntil } from 'rxjs';
import { TranslatePipe } from '@ngx-translate/core';
import { I } from '@angular/cdk/a11y-module.d-DBHGyKoh';

@Component({
  selector: 'app-orders-management',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule, 
    FormsModule, 
    PaginationComponent, 
    DatePipe,
    CurrencyFormatPipe,
    TranslatePipe
  ],
  templateUrl: './orders-management.component.html'
})
export class OrdersManagementComponent implements OnInit , OnDestroy {
  private orderService = inject(OrderService);
  orders = this.orderService.orders$;
  searchTerm = signal('');
  sortByDate = signal<'asc' | 'desc' | ''>('');
  sortByTotal = signal<'asc' | 'desc' | ''>('');
  selectedStatus: WritableSignal<ORDER_STATUS_TYPES> = signal('ALL');
  pagination = this.orderService.pagination;
  totalPages = this.orderService.totalPages;
  totalElements = this.orderService.totalElements;
  totalAmmount = this.orderService.totalAmmount;
  globalTotalElements = this.orderService.globalTotalElements;
  currentWeekOrders = this.orderService.currentWeekOrders;
  lastWeekOrders = this.orderService.lastWeekOrders;
  currentWeekOrdersTotalAmount = this.orderService.currentWeekOrdersTotalAmount;
  lastWeekOrdersTotalAmount = this.orderService.lastWeekOrdersTotalAmount;
  isLoading = true;
  errorMessage = '';
  private destroy$ = new Subject<void>();
  // Metrics
  ordersChangePercentage = 0;
  ordersTotalAmountChangePercentage = 0;
  orderStatus =ORDER_STATUS_RECORDS
  constructor(
    private dialog: MatDialog,
    private router: Router,
  ) {
    effect(() => {
      
      //this.calculateMetrics(); // reacts to filteredOrders
    });
    effect(() => {
      this.selectedStatus();
      this.searchTerm();
      untracked(() => this.resetCurrentPage());
      
    })
    effect(() => {
      this.pagination();
      this.sortByDate();
      untracked(() => this.loadOrders());
    })
  }

  ngOnInit(): void {
    this.loadOrders();    
  }




  onPageChange(newPage: number): void {
    this.orderService.updateCurrentPage(newPage);
  }


  sortOrdersByDate(): void {
    this.sortByTotal.set('');
    const current = this.sortByDate();
    this.sortByDate.set(current === 'asc' ? 'desc' : current === 'desc' ? 'asc' : 'desc');
  }

  sortOrdersByTotal(): void {
    this.sortByDate.set('');
    const current = this.sortByTotal();
    this.sortByTotal.set(current === 'asc' ? 'desc' : current === 'desc' ? 'asc' : 'desc');
  }

  calculatePercentageChangeOrderNumber = computed(() => {
    const current = this.currentWeekOrders();
    const previous = this.lastWeekOrders();
    return this.calculatePercentageChange(current, previous);
  });
  calculatePercentageChangeOrderAmmount = computed(() => {
    const current = this.currentWeekOrdersTotalAmount();
    const previous = this.lastWeekOrdersTotalAmount();
    return this.calculatePercentageChange(current, previous);
  });
  private calculatePercentageChange(current: number, previous: number): number {
    if (previous === 0) return current > 0 ? 100 : 0;
    return ((current - previous) / previous) * 100;
  }
  hasIncreased(current: number, last: number): boolean {
    return current > last;
  }

  loadOrders(): void {
    this.isLoading = false;
    const params = {
      page: this.pagination().page,
      size: this.pagination().size ,
      status: this.selectedStatus()  ,
      email: this.searchTerm() ,
      orderByCreationDateDesc: this.sortByDate() === 'desc'
    }
    this.orderService.getPagedFilterdOrders(params);
  }

  viewOrderDetails(id: number): void {
    this.router.navigate(['/admin/orders', id]);
  }

  cancelOrder(id: number): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Cancel Order',
        message: 'Are you sure you want to cancel this order?'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.orderService.deleteOrder(id).subscribe({
          next: () => {
            this.loadOrders();
          },
          error: (err) => {
            this.errorMessage = 'Failed to cancel order';
          }
        });
      }
    });
  }
  openCreateOrder(){
    const dialogRef = this.dialog.open(OrderModalComponent);
    dialogRef.afterClosed().pipe(
          takeUntil(this.destroy$)).subscribe((isUpdated) => {
            if(isUpdated)
              this.loadOrders();
          });
  }
  resetCurrentPage(): void {
    this.orderService.resetCurrentPage();
    this.loadOrders();
  }
  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}