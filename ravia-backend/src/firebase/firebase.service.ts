import { Injectable, OnModuleInit, Logger } from '@nestjs/common';
import * as admin from 'firebase-admin';

@Injectable()
export class FirebaseService implements OnModuleInit {
  private readonly logger = new Logger(FirebaseService.name);
  private app: admin.app.App;

  onModuleInit() {
    if (admin.apps.length > 0) {
      this.app = admin.apps[0];
      this.configureFirestore();
      return;
    }

    const serviceAccountPath = process.env.FIREBASE_SERVICE_ACCOUNT_PATH;
    const serviceAccountJson = process.env.FIREBASE_SERVICE_ACCOUNT_JSON;

    let credential: admin.credential.Credential;

    if (serviceAccountJson) {
      const parsed = JSON.parse(serviceAccountJson);
      credential = admin.credential.cert(parsed);
    } else if (serviceAccountPath) {
      credential = admin.credential.cert(serviceAccountPath);
    } else {
      // Use application default credentials (Cloud Run / App Engine)
      credential = admin.credential.applicationDefault();
    }

    this.app = admin.initializeApp({
      credential,
      storageBucket: process.env.FIREBASE_STORAGE_BUCKET,
      projectId: process.env.FIREBASE_PROJECT_ID,
    });
    this.configureFirestore();

    this.logger.log('Firebase Admin SDK initialized');
  }

  private configureFirestore() {
    try {
      this.app.firestore().settings({ ignoreUndefinedProperties: true });
    } catch {
      // Firestore settings can only be applied before the first operation.
    }
  }

  get auth(): admin.auth.Auth {
    return this.app.auth();
  }

  get firestore(): admin.firestore.Firestore {
    return this.app.firestore();
  }

  get storage(): admin.storage.Storage {
    return this.app.storage();
  }

  get messaging(): admin.messaging.Messaging {
    return this.app.messaging();
  }

  collection(path: string): admin.firestore.CollectionReference {
    return this.firestore.collection(path);
  }

  async verifyIdToken(token: string): Promise<admin.auth.DecodedIdToken> {
    return this.auth.verifyIdToken(token);
  }
}
