import { Module } from '@nestjs/common';
import { RiskZonesService } from './risk-zones.service';
import { RiskZonesController } from './risk-zones.controller';
import { UsersModule } from '../users/users.module';

@Module({
  imports: [UsersModule],
  providers: [RiskZonesService],
  controllers: [RiskZonesController],
  exports: [RiskZonesService],
})
export class RiskZonesModule {}
