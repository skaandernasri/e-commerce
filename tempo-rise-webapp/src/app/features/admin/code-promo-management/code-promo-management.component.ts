import { Component } from '@angular/core';
import { CodePromo } from '../../../core/models/code-promo';
import { CodePromoService } from '../../../core/services/code-promo.service';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmDialogComponent } from '../../../shared/components/dialogs/confirm-dialog/confirm-dialog.component';
import { CodePromoModalComponent } from './code-promo-modal/code-promo-modal.component';
import { DatePipe } from '@angular/common';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-code-promo-management',
  imports: [ReactiveFormsModule,FormsModule,PaginationComponent,DatePipe],
  templateUrl: './code-promo-management.component.html',
  styleUrl: './code-promo-management.component.css',
})
export class CodePromoManagementComponent {
  codes: CodePromo[] = [];
  filteredCodes: CodePromo[] = [];
  searchTerm = '';
  isLoading = true;
  errorMessage = '';
  selectedCode: string = 'All';
  status:string[]=['Active','inActive','All'];
  statusMessage:string =''
  currentPage: number = 1;
  itemsPerPage: number = 10;
  constructor(
    private codePromoService: CodePromoService,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.loadCodes();
  }

  loadCodes(): void {
    this.isLoading = true;
    this.codePromoService.getAllCodePromo().subscribe({

      next: (codes) => {
        this.codes = codes;
        this.filteredCodes = [...codes];
        this.isLoading = false;
        
      },
      error: (err) => {
        //console.log(err);
        console.error('Error loading codes:', err);
        if(err.status==404){
          this.codes=[]
          this.filteredCodes = [...this.codes];
        }
        else
          this.errorMessage = 'Failed to load codes';
        this.isLoading = false;
      }
    });
  }

   filterCodes() {
    if(this.selectedCode === 'All') {
      this.filteredCodes = [...this.codes];
    } else if(this.selectedCode === 'Active') {
      this.isLoading=true
      this.codePromoService.getActiveCode().subscribe({
        next: (codes) => {
          this.filteredCodes = [...codes];
          this.isLoading=false

        },
        error: (err) => {
          this.isLoading=false
          this.errorMessage = 'Failed to load codes';
          
        }
      })
    }
    else{ 
      this.codePromoService.getInActiveCode().subscribe({
        next: (codes) => {
          this.filteredCodes = [...codes];
        },
        error: (err) => {
          this.errorMessage = 'Failed to load codes';
        }
      })
        }
        this.currentPage=1;
        this.paginatedCodes;


   }

  // editCategory(categoryId: number): void {
  //   this.router.navigate([`/admin/categories/edit/${categoryId}`]);
  // }

  deleteCodes(id: number): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Delete Code',
        message: 'Are you sure you want to delete this code?'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.codePromoService.deleteCodePromo(id).subscribe({
          next: () => {
            this.loadCodes();
          },
          error: (err) => {
            this.errorMessage = 'Failed to delete code';
          }
        });
      }
    });
  }
  // Open modal to add category
  openCreateCode(): void {
    const dialogRef = this.dialog.open(CodePromoModalComponent, {
      data: { code: null },
     // panelClass: ['rounded-lg', 'shadow-xl'] ,
      //backdropClass: 'transparent-backdrop',
      disableClose: false, // This allows closing by clicking outside
      autoFocus: false,
      restoreFocus: false,
      ariaModal: false,


    });
  
    dialogRef.afterClosed().subscribe(result => {
      if (result) this.loadCodes();
    });
  }
  
  editCode(id: number): void {
    if(id==null)return;
    const code = this.filteredCodes.find(c => c.id === id);
    if (code) {
      const dialogRef = this.dialog.open(CodePromoModalComponent, {
        data: { code: code },
        restoreFocus: false,
        panelClass: ['rounded-lg', 'shadow-xl'], // Add Tailwind-like classes
        disableClose: false,
        autoFocus: false,
        ariaModal: false,
      });
  
      dialogRef.afterClosed().subscribe(result => {
        if (result) {
          this.loadCodes();
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
  
get paginatedCodes() {
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    return this.filteredCodes.slice(startIndex, startIndex + this.itemsPerPage);
  }
}
