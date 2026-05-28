import { Module } from '@nestjs/common';
import { MissingPersonsService } from './missing-persons.service';
import { MissingPersonsController } from './missing-persons.controller';
import { UsersModule } from '../users/users.module';

@Module({
  imports: [UsersModule],
  providers: [MissingPersonsService],
  controllers: [MissingPersonsController],
})
export class MissingPersonsModule {}
