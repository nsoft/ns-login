import {Injectable} from '@angular/core';
import {CollectionViewer} from '@angular/cdk/collections';
import {BehaviorSubject, Observable, of} from 'rxjs';
import {NSRESTService} from '../ns-rest.service';
import {catchError, finalize} from 'rxjs/operators';
import {User} from '../model/User';

@Injectable({
  providedIn: 'root'
})
export class UserDataSourceService {

  private usersSubject = new BehaviorSubject<User[]>([]);

  private loadingSubject = new BehaviorSubject<boolean>(false);

  public loading$ = this.loadingSubject.asObservable();

  constructor(private rest: NSRESTService) { }

  // noinspection JSUnusedLocalSymbols
  connect(collectionViewer: CollectionViewer): Observable<any[]|[]> {
    return this.usersSubject.asObservable();
  }

  // noinspection JSUnusedLocalSymbols
  disconnect(collectionViewer: CollectionViewer): void {
    this.usersSubject.complete();
    this.loadingSubject.complete();
  }

  loadUsers(start = 0,
            rows = 5, filter = '', sort = 'asc') {

    this.loadingSubject.next(true);

    this.rest.find('AppUser', filter, sort, start, rows)
      .pipe(
        catchError(() => of([])),
        finalize(() => this.loadingSubject.next(false))
      )
      .subscribe(users => this.usersSubject.next(users));
  }

  data() {
    return this.usersSubject.getValue();
  }
}
