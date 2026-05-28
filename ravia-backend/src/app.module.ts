import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { ThrottlerModule } from '@nestjs/throttler';
import { FirebaseModule } from './firebase/firebase.module';
import { AuthModule } from './auth/auth.module';
import { UsersModule } from './users/users.module';
import { ReportsModule } from './reports/reports.module';
import { ReportConfirmationsModule } from './report-confirmations/report-confirmations.module';
import { AlertsModule } from './alerts/alerts.module';
import { RiskZonesModule } from './risk-zones/risk-zones.module';
import { MissingPersonsModule } from './missing-persons/missing-persons.module';
import { ChatbotModule } from './chatbot/chatbot.module';
import { AiAnalysisModule } from './ai-analysis/ai-analysis.module';
import { NotificationsModule } from './notifications/notifications.module';
import { StatisticsModule } from './statistics/statistics.module';
import { HealthController } from './health.controller';

@Module({
  imports: [
    ConfigModule.forRoot({ isGlobal: true, envFilePath: '.env' }),
    ThrottlerModule.forRoot([
      {
        ttl: parseInt(process.env.THROTTLE_TTL ?? '60000'),
        limit: parseInt(process.env.THROTTLE_LIMIT ?? '100'),
      },
    ]),
    FirebaseModule,
    AuthModule,
    UsersModule,
    ReportsModule,
    ReportConfirmationsModule,
    AlertsModule,
    RiskZonesModule,
    MissingPersonsModule,
    ChatbotModule,
    AiAnalysisModule,
    NotificationsModule,
    StatisticsModule,
  ],
  controllers: [HealthController],
})
export class AppModule {}
