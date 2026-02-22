import { Component, inject, OnInit, AfterViewInit, effect, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProductService } from '../../../core/services/product.service';
import { OrderService } from '../../../core/services/order.service';
import { UsersService } from '../../../core/services/users.service';
import { Chart, registerables } from 'chart.js';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
})
export class DashboardComponent implements OnInit, AfterViewInit, OnDestroy {
  private productService = inject(ProductService);
  private orderService = inject(OrderService);
  private userService = inject(UsersService);
  users = this.userService.users$;
  orders = this.orderService.orders$;
  totalProducts=0;
  private ordersStatusChart: Chart | null = null;
  private productsStockChart: Chart | null = null;
  private monthlySalesChart: Chart | null = null;
  private paymentMethodsChart: Chart | null = null;
  
  constructor() {
    Chart.register(...registerables);
    effect(() => { 
      const orders = this.orders();
      this.updateCharts();
    });
  }

  ngOnInit(): void {
    this.orderService.getAllOrders();
    this.loadUsers();
    this.loadProductStock();
  }

  ngAfterViewInit(): void {
    this.updateCharts();
  }

  ngOnDestroy(): void {
    this.destroyOrdersCharts();
    this.destroyProductsStockChart();
  }

  loadUsers(): void {
    this.userService.getAllUsers().subscribe({
      error: (error) => console.error(error)
    });
  }

  private updateCharts(): void {
    if (this.orders()) {
      this.destroyOrdersCharts();
      this.initOrdersCharts(this.orders());
    }
  }

  private destroyOrdersCharts(): void {
    if (this.ordersStatusChart) {
      this.ordersStatusChart.destroy();
      this.ordersStatusChart = null;
    }
    if (this.monthlySalesChart) {
      this.monthlySalesChart.destroy();
      this.monthlySalesChart = null;
    }
    if (this.paymentMethodsChart) {
      this.paymentMethodsChart.destroy();
      this.paymentMethodsChart = null;
    }
  }
  private destroyProductsStockChart(): void {
    if (this.productsStockChart) {
      this.productsStockChart.destroy();
      this.productsStockChart = null;
    }
  }

  private initOrdersCharts(orders: any[]): void {
    this.initOrdersStatusChart(orders);
    this.initMonthlySalesChart(orders);
    this.initPaymentMethodsChart(orders);
  }
  

  private initOrdersStatusChart(orders: any[]): void {
    const statusCounts = {
      InProgress: orders.filter(o => o.statut === 'EN_COURS').length,
      Shipped: orders.filter(o => o.statut === 'EXPEDIEE').length,
      Delivered: orders.filter(o => o.statut === 'LIVREE').length,
    };

    const ctx = document.getElementById('ordersStatusChart') as HTMLCanvasElement;
    if (ctx) {
      this.ordersStatusChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
          labels: Object.keys(statusCounts),
          datasets: [{
            data: Object.values(statusCounts),
            backgroundColor: [
              'rgba(255, 206, 86, 0.7)',
              'rgba(54, 162, 235, 0.7)',
              'rgba(75, 192, 192, 0.7)',
              'rgba(255, 99, 132, 0.7)'
            ],
            borderColor: [
              'rgba(255, 206, 86, 1)',
              'rgba(54, 162, 235, 1)',
              'rgba(75, 192, 192, 1)',
              'rgba(255, 99, 132, 1)'
            ],
            borderWidth: 1
          }]
        },
        options: {
          responsive: true,
          plugins: {
            legend: {
              position: 'bottom',
            }
          }
        }
      });
    }
  }

 private loadProductStock(): void {
  this.productService.getProductsStockDetails(10, 10, 0).subscribe({
    next: (response) => {
      this.totalProducts=response.totalProducts;
      this.initProductsStockChart(response);
    },
    error: (error) => console.error('Error loading product stock:', error)
  });
}

private initProductsStockChart(stockData: any): void {
  // Destroy previous chart if exists
  if (this.productsStockChart) {
    this.productsStockChart.destroy();
    this.productsStockChart = null;
  }

  const ctx = document.getElementById('productsStockChart') as HTMLCanvasElement;
  if (!ctx) {
    console.error('Canvas element not found!');
    return;
  }

  this.productsStockChart = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: ['In Stock (>10)', 'Low Stock (1-10)', 'Out of Stock'],
      datasets: [{
        label: 'Products',
        data: [stockData.inStock, stockData.lowStock, stockData.outOfStock],
        backgroundColor: [
          'rgba(75, 192, 192, 0.7)',
          'rgba(255, 206, 86, 0.7)',
          'rgba(255, 99, 132, 0.7)'
        ],
        borderColor: [
          'rgba(75, 192, 192, 1)',
          'rgba(255, 206, 86, 1)',
          'rgba(255, 99, 132, 1)'
        ],
        borderWidth: 1
      }]
    },
    options: {
      responsive: true,
      scales: {
        y: {
          beginAtZero: true
        }
      }
    }
  });
}

  private initMonthlySalesChart(orders: any[]): void {
    const monthlySales = Array(12).fill(0);
    orders.forEach(order => {
      const month = new Date(order.date).getMonth();
      monthlySales[month] += order.total;
    });

    const ctx = document.getElementById('monthlySalesChart') as HTMLCanvasElement;
    if (ctx) {
      this.monthlySalesChart = new Chart(ctx, {
        type: 'line',
        data: {
          labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
          datasets: [{
            label: 'Sales',
            data: monthlySales,
            fill: false,
            backgroundColor: 'rgba(153, 102, 255, 0.7)',
            borderColor: 'rgba(153, 102, 255, 1)',
            tension: 0.1,
            borderWidth: 2
          }]
        },
        options: {
          responsive: true,
          scales: {
            y: {
              beginAtZero: false
            }
          }
        }
      });
    }
  }

  private initPaymentMethodsChart(orders: any[]): void {
    const paymentMethods = {
      'Credit Card': orders.filter(o => o.modePaiement === 'CARTE_BANCAIRE').length,
      'PayPal': orders.filter(o => o.modePaiement === 'PAYPAL').length,
      'Bank Transfer': orders.filter(o => o.modePaiement === 'VIREMENT').length,
      'Cash on Delivery': orders.filter(o => o.modePaiement === 'A_LIVRAISON').length
    };

    const ctx = document.getElementById('paymentMethodsChart') as HTMLCanvasElement;
    if (ctx) {
      this.paymentMethodsChart = new Chart(ctx, {
        type: 'pie',
        data: {
          labels: Object.keys(paymentMethods),
          datasets: [{
            data: Object.values(paymentMethods),
            backgroundColor: [
              'rgba(54, 162, 235, 0.7)',
              'rgba(255, 99, 132, 0.7)',
              'rgba(255, 159, 64, 0.7)',
              'rgba(75, 192, 192, 0.7)'
            ],
            borderColor: [
              'rgba(54, 162, 235, 1)',
              'rgba(255, 99, 132, 1)',
              'rgba(255, 159, 64, 1)',
              'rgba(75, 192, 192, 1)'
            ],
            borderWidth: 1
          }]
        },
        options: {
          responsive: true,
          plugins: {
            legend: {
              position: 'bottom',
            }
          }
        }
      });
    }
  }
}