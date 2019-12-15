import {Component, OnInit} from '@angular/core';
import {UserDataSourceService} from "./user-data-source.service";
import {NSRESTService} from "../ns-rest.service";

@Component({
  selector: 'app-user-table',
  templateUrl: './user-table.component.html',
  styleUrls: ['./user-table.component.scss']
})
export class UserTableComponent implements OnInit {

  constructor(private nsrestService: NSRESTService) {}


  dataSource: UserDataSourceService;
  displayedColumns= ["id", "name"];

  ngOnInit() {
    this.dataSource = new UserDataSourceService(this.nsrestService);
    this.dataSource.loadLessons(1);
  }
}
