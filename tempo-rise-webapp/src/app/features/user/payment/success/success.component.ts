import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { OrderService } from '../../../../core/services/order.service';
import { PaymentDetails } from '../../../../core/models/order';
import { CommonModule } from '@angular/common';
import { CurrencyFormatPipe } from '../../../../core/pipe/currency-pipe.pipe';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-success',
  imports: [CommonModule,RouterLink, CurrencyFormatPipe],
  templateUrl: './success.component.html',
  styleUrl: './success.component.css'
})
export class SuccessComponent implements OnInit {
  authService = inject(AuthService)
  authState= this.authService.authState
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
