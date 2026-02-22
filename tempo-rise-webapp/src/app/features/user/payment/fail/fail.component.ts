import { Component, inject, OnInit } from '@angular/core';
import { PaymentDetails } from '../../../../core/models/order';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { OrderService } from '../../../../core/services/order.service';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../../core/services/auth.service';
import { CurrencyFormatPipe } from '../../../../core/pipe/currency-pipe.pipe';

@Component({
  selector: 'app-fail',
  imports: [CommonModule,RouterLink, CurrencyFormatPipe],
  templateUrl: './fail.component.html',
  styleUrl: './fail.component.css'
})
export class FailComponent implements OnInit{
  authService = inject(AuthService);
  authState= this.authService.authState;
 payment?: PaymentDetails['payment'];
  constructor(private route: ActivatedRoute,
    private orderService: OrderService
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const payRef = params['payment_ref'];
      this.loadPaymentDetails(payRef);
    });
  }
loadPaymentDetails(payRef: string): void {
  if(this.authState().isAuthenticated){
    this.orderService.getPaymentRef(payRef,this.authState().id).subscribe({
      next: (data) => {
        this.payment = data['payment'];
        this.orderService.getOrderById(+this.payment.orderId).subscribe({
          next: (order) => {
            this.payment!.convertedAmount = order.total;
          }
        })
      },
      error: (err) => {
        console.error(err);
      }
    }
    );
  }
  else{
 this.orderService.getPaymentRef(payRef).subscribe({
      next: (data) => {
        this.payment = data['payment'];
        this.orderService.getOrderById(+this.payment.orderId).subscribe({
          next: (order) => {
            this.payment!.convertedAmount = order.total;
          }
        })
      },
      error: (err) => {
        console.error(err);
      }
    }
    );
  }

  }
}
