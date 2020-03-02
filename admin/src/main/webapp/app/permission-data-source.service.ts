import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable, of} from 'rxjs';
import {NSRESTService} from './ns-rest.service';
import {CollectionViewer} from '@angular/cdk/collections';
import {catchError, tap} from 'rxjs/operators';
import {Permission} from './model/Permission';

@Injectable({
  providedIn: 'root'
})
export class PermissionDataSourceService {


  private permissionsSubject = new BehaviorSubject<Permission[]>([]);

  private loadingSubject = new BehaviorSubject<boolean>(false);

  public loading$ = this.loadingSubject.asObservable();

  constructor(private rest: NSRESTService) {
  }

  // noinspection JSUnusedLocalSymbols
  connect(collectionViewer: CollectionViewer): Observable<any[] | []> {
    return this.permissionsSubject.asObservable();
  }

  // noinspection JSUnusedLocalSymbols
  disconnect(collectionViewer: CollectionViewer): void {
    this.permissionsSubject.complete();
    this.loadingSubject.complete();
  }

  loadPermissions(start = 0,
                  rows = 99999, filter = '', sort = 'asc'): Observable<Permission[]> {

    this.loadingSubject.next(true);

    return this.rest.find('Permission', filter, sort, start, rows)
      .pipe(
        catchError(() => of([])),
        tap(roles => this.permissionsSubject.next(roles))
      );
  }

  data() {
    return this.permissionsSubject.getValue();
  }
}
