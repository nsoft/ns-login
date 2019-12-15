import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {Observable} from "rxjs";
import {map} from "rxjs/operators";

@Injectable({
  providedIn: 'root'
})
export class NSRESTService {

  constructor(private http:HttpClient) { }

  find(
    type:string, filter = '', sortOrder = '',
    start = 0, rows = 3):  Observable<[]> {

    return this.http.get('/rest/api/' + type, {
      params: new HttpParams()
        .set('filter', filter)
        .set('sortOrder', sortOrder)
        .set('start', start.toString())
        .set('rows', rows.toString())
    }).pipe(
      map(res =>  res["results"])
    );
  }
}
