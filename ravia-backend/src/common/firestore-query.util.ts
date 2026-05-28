import { Logger } from '@nestjs/common';
import * as admin from 'firebase-admin';

type FirestoreError = {
  code?: unknown;
  details?: unknown;
  message?: unknown;
};

export type FirestoreQueryResult = {
  snapshot: admin.firestore.QuerySnapshot;
  usedFallback: boolean;
};

export function isMissingFirestoreIndex(error: unknown): boolean {
  const candidate = error as FirestoreError;
  const text = `${candidate?.message ?? ''} ${candidate?.details ?? ''}`;

  return candidate?.code === 9 && text.includes('requires an index');
}

export async function getWithIndexFallback(
  indexedQuery: admin.firestore.Query,
  fallbackQuery: admin.firestore.Query,
  logger: Logger,
  context: string,
): Promise<FirestoreQueryResult> {
  try {
    return {
      snapshot: await indexedQuery.get(),
      usedFallback: false,
    };
  } catch (error) {
    if (!isMissingFirestoreIndex(error)) {
      throw error;
    }

    logger.warn(`${context}: missing Firestore index, using local sort fallback`);
    return {
      snapshot: await fallbackQuery.get(),
      usedFallback: true,
    };
  }
}
