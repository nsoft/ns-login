import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable, PartialObserver, throwError} from 'rxjs';
import {catchError, map} from 'rxjs/operators';
import {Router} from '@angular/router';

declare const JSOG: any; // Using javascript JSOG due to https://github.com/emundo/jsog-typescript/issues/8

@Injectable({
  providedIn: 'root'
})
export class NSRESTService {

  constructor(private http: HttpClient, private router: Router) {
  }

  get(
    type: string, id): Observable<any[]> {

    return this.http.get('/rest/api/' + type + '/' + id).pipe(
      // tslint:disable-next-line:no-string-literal
      map(res => res['results'].map(r => JSOG.decode(r)))
    );
  }

  find(
    type: string, filter = '', sortOrder = '',
    start = 0, rows = 3): Observable<[]> {

    return this.http.get('/rest/api/' + type, {
      params: new HttpParams()
        .set('filter', filter)
        .set('sortOrder', sortOrder)
        .set('start', start.toString())
        .set('rows', rows.toString())
    }).pipe(
      catchError(err => {
        if (err.status === 401) {
          this.router.navigate(['/?logout=true']);
        }
        const error = err.error.message || err.statusText;
        return throwError(error);
      }),
      // tslint:disable-next-line:no-string-literal
      map(res => JSOG.decode(res)['results'])
    );
  }

  update(type: string, object, obs?: PartialObserver<any>): Observable<any> {
    const body = JSOG.encode(object);

    const objectObservable = this.http.post('/rest/api/' + type + '/' + object.id, body);
    if (obs) {
      objectObservable.subscribe(obs);
    }
    return objectObservable;
  }
}
