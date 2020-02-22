import {Injectable} from '@angular/core';
import {CollectionViewer} from "@angular/cdk/collections";
import {BehaviorSubject, Observable, of} from "rxjs";
import {NSRESTService} from "../ns-rest.service";
import {catchError, finalize} from "rxjs/operators";

@Injectable({
  providedIn: 'root'
})
export class UserDataSourceService {

  private lessonsSubject = new BehaviorSubject<any[]|[]>([]);

  private loadingSubject = new BehaviorSubject<boolean>(false);

  public loading$ = this.loadingSubject.asObservable();

  constructor(private rest: NSRESTService) { }

  // noinspection JSUnusedLocalSymbols
  connect(collectionViewer: CollectionViewer): Observable<any[]|[]> {
    return this.lessonsSubject.asObservable();
  }

  // noinspection JSUnusedLocalSymbols
  disconnect(collectionViewer: CollectionViewer): void {
    this.lessonsSubject.complete();
    this.loadingSubject.complete();
  }

  loadUsers(courseId: number, filter = '',
            sort = 'asc', start = 0, rows = 5) {

    this.loadingSubject.next(true);

    this.rest.find("AppUser", filter, sort, start, rows).pipe(
      catchError(() => of([])),
      finalize(() => this.loadingSubject.next(false))
    )
      .subscribe(lessons => this.lessonsSubject.next(lessons));
  }
}
