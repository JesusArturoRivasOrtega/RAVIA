import { Module } from '@nestjs/common';
import { ReportsService } from './reports.service';
import { ReportsController } from './reports.controller';
import { UsersModule } from '../users/users.module';
import { AiAnalysisModule } from '../ai-analysis/ai-analysis.module';
import { NotificationsModule } from '../notifications/notifications.module';

@Module({
  imports: [UsersModule, AiAnalysisModule, NotificationsModule],
  providers: [ReportsService],
  controllers: [ReportsController],
  exports: [ReportsService],
})
export class ReportsModule {}
