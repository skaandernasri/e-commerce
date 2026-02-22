import { Component } from '@angular/core';
import { Promotion } from '../../../core/models/promotion';
import { PromotionService } from '../../../core/services/promotion.service';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmDialogComponent } from '../../../shared/components/dialogs/confirm-dialog/confirm-dialog.component';
import { PromotionModalComponent } from './promotion-modal/promotion-modal.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';
import { DatePipe } from '@angular/common';
@Component({
  selector: 'app-promotions-management',
  imports: [ReactiveFormsModule,FormsModule,PaginationComponent,DatePipe],
  templateUrl: './promotions-management.component.html',
  styleUrl: './promotions-management.component.css'
})
export class PromotionsManagementComponent {
  promotions: Promotion[] = [];
  filteredPromotions: Promotion[] = [];
  searchTerm = '';
  isLoading = true;
  errorMessage = '';
  selectedPomotion: string = 'All';
  status:string[]=['Active','inActive','All'];
  statusMessage:string =''
  currentPage: number = 1;
  itemsPerPage: number = 10;
  constructor(
    private promotionService: PromotionService,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.loadPromotions();
  }

  loadPromotions(): void {
    this.isLoading = true;
    this.promotionService.getAllPromotions().subscribe({
      next: (promotions) => {
        this.promotions = promotions;
        //this.promotions.map(pro=>(new Date(pro.dateDebut).getTime()<=Date.now()&& new Date(pro.dateFin).getTime()>Date.now()) ? pro.status="Active" : pro.status="inActive" )
        this.filteredPromotions = this.promotions.map(promo=>({
          ...promo,
          status:(new Date(promo.dateDebut).getTime()<=Date.now()&& new Date(promo.dateFin).getTime()>Date.now()?"Active":"inActive")
        }))
        
        this.isLoading = false;
        this.selectedPomotion='All'
      },
      error: (err) => {
        if(err.status==404){
          this.promotions=[]
          this.filteredPromotions = [...this.promotions];
        }
        else
          this.errorMessage = 'Failed to load promotions';
        this.isLoading = false;
      }
    });
  }

   filterPromotions() {

   if(this.selectedPomotion === 'Active') {
      this.isLoading=true
      this.promotionService.getActivePromotions().subscribe({
        next: (proms) => {
          this.filteredPromotions = proms.map(promo => ({
            ...promo,
            status: 'Active'
          }));
          this.isLoading=false

        },
        error: (err) => {
          this.isLoading=false
          this.errorMessage = 'Failed to load codes';
          
        }
      })
    }
    else if(this.selectedPomotion === 'inActive'){ 
      this.isLoading=true
      this.promotionService.getInActivePromotions().subscribe({
        next: (proms) => {
          this.filteredPromotions = proms.map(promo => ({
            ...promo,
            status: 'inActive'
          }));
          this.isLoading=false

        },
        error: (err) => {
          this.errorMessage = 'Failed to load codes';
        }
      })
        }
        else{
          this.loadPromotions()
        }
        this.currentPage=1;
        this.paginatedPromotions;
    

   }

  deletePromotion(id: number): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Delete Promotion',
        message: 'Are you sure you want to delete this promotion?'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.promotionService.deletePromotion(id).subscribe({
          next: () => {
            this.filterPromotions();
          },
          error: (err) => {
            this.errorMessage = 'Failed to delete promotion';
          }
        });
      }
    });
  }
  // Open modal to add category
  openCreatePromotion(): void {
    const dialogRef = this.dialog.open(PromotionModalComponent, {
      data: { promotion: null },
     // panelClass: ['rounded-lg', 'shadow-xl'] ,
      //backdropClass: 'transparent-backdrop',
     // disableClose: true
    });
  
    dialogRef.afterClosed().subscribe(result => {
      if (result) this.filterPromotions();
    });
  }
  
  editPromotion(id: number): void {
    const promotion = this.promotions.find(c => c.id === id);
    if (promotion) {
      const dialogRef = this.dialog.open(PromotionModalComponent, {
        data: { promotion: promotion },
        panelClass: ['rounded-lg', 'shadow-xl'] // Add Tailwind-like classes
      });
  
      dialogRef.afterClosed().subscribe(result => {
        if (result) {
          this.filterPromotions();
        }
      });
    }
  }
  onPageChange(newPage: number): void {
    this.currentPage = newPage;
  }
  calculateDateDuration(dateDebut: string, dateFin: string): string {
    const startDate = new Date(dateDebut);
    const endDate = new Date(dateFin);
  
    const durationMs = endDate.getTime() - startDate.getTime();
  
    const totalMinutes = Math.floor(durationMs / (1000 * 60));
    const days = Math.floor(totalMinutes / (60 * 24));
    const hours = Math.floor((totalMinutes % (60 * 24)) / 60);
    const minutes = totalMinutes % 60;
  
    return `${days}d ${hours}h ${minutes}min`;
  }
  expiresIn(expireDate: string): string {
    const endDate = new Date(expireDate);
    const startDate = Date.now();
    const durationMs = endDate.getTime() - startDate;
  
    if (durationMs < 0) return 'Expired';
  
    const days = Math.floor(durationMs / (1000 * 60 * 60 * 24));
    const hours = Math.floor((durationMs % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
    const minutes = Math.floor((durationMs % (1000 * 60 * 60)) / (1000 * 60));
  
    return `${days}d ${hours}h ${minutes}min`;
  }
  
  
get paginatedPromotions() {
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    return this.filteredPromotions.slice(startIndex, startIndex + this.itemsPerPage);
  }
}
