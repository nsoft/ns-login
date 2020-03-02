import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable, of} from 'rxjs';
import {NSRESTService} from './ns-rest.service';
import {CollectionViewer} from '@angular/cdk/collections';
import {catchError, tap} from 'rxjs/operators';
import {Role} from './model/Role';

@Injectable({
  providedIn: 'root'
})
export class RoleDataSourceService {


  private rolesSubject = new BehaviorSubject<Role[]>([]);

  private loadingSubject = new BehaviorSubject<boolean>(false);

  public loading$ = this.loadingSubject.asObservable();

  constructor(private rest: NSRESTService) {
  }

  // noinspection JSUnusedLocalSymbols
  connect(collectionViewer: CollectionViewer): Observable<any[] | []> {
    return this.rolesSubject.asObservable();
  }

  // noinspection JSUnusedLocalSymbols
  disconnect(collectionViewer: CollectionViewer): void {
    this.rolesSubject.complete();
    this.loadingSubject.complete();
  }

  loadRoles(start = 0,
            rows = 99999, filter = '', sort = 'asc'): Observable<Role[]> {

    this.loadingSubject.next(true);

    return this.rest.find('Role', filter, sort, start, rows)
      .pipe(
        catchError(() => of([])),
        tap(roles => this.rolesSubject.next(roles))
      );
  }

  data() {
    return this.rolesSubject.getValue();
  }

}
