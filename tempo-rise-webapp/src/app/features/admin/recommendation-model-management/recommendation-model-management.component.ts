import { Component } from '@angular/core';
import { RecommendationService } from '../../../core/services/recommendation.service';
import { CommonModule, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-recommendation-model-management',
  imports: [DecimalPipe,CommonModule,FormsModule],
  templateUrl: './recommendation-model-management.component.html',
  styleUrl: './recommendation-model-management.component.css'
})
export class RecommendationModelManagementComponent {
loading = false;
metrics: any = null;
history: any[] = [];
currentConfig: any = null;
message: string = '';
bestconfig: any = null;
trainParams = {
  loss: 'logistic',
  learning_rate: 0.005,
  epochs: 60,
  no_components: 32
};

tuneParams = {
  n_trials: 30,
  no_components: 32
};

constructor(private recommendationService: RecommendationService) {}
trainModel() {
  this.loading = true;
  this.recommendationService.train(
    this.trainParams.loss,
    this.trainParams.learning_rate,
    this.trainParams.epochs,
    this.trainParams.no_components
  ).subscribe({
    next: (result) => {
      
      this.message = result.message;
      this.currentConfig = result.config;
      this.loading = false;
    },
    error: (err) => {
      console.error(err);
      this.loading = false;
    }
  });
}

tuneModel() {
  this.loading = true;
  this.recommendationService.tune(
    this.tuneParams.n_trials,
    this.tuneParams.no_components
  ).subscribe({
    next: (result) => {
      this.bestconfig = result.best_config;
      
      this.message = result.message;
      this.currentConfig = result.best_config;
      this.loading = false;
    },
    error: (err) => {
      console.error(err);
      this.loading = false;
    }
  });
}

getMetrics() {
  this.recommendationService.metrics().subscribe({
    next: (result) => {
      this.metrics = result.data.metrics;
      this.currentConfig = result.data.config;

      // Add this run to history
      this.history.unshift({
        ...result.data.metrics,
        date: new Date().toLocaleString()
      });
    },
    error: (err) => {
      console.error(err);
    }
  });
}


}
