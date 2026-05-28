import { Module } from '@nestjs/common';
import { ReportConfirmationsService } from './report-confirmations.service';
import { ReportConfirmationsController } from './report-confirmations.controller';
import { UsersModule } from '../users/users.module';
import { NotificationsModule } from '../notifications/notifications.module';

@Module({
  imports: [UsersModule, NotificationsModule],
  providers: [ReportConfirmationsService],
  controllers: [ReportConfirmationsController],
})
export class ReportConfirmationsModule {}
