import {Component, OnInit} from '@angular/core';
import {UserDataSourceService} from './user-data-source.service';
import {NSRESTService} from '../ns-rest.service';
import {SelectionModel} from '@angular/cdk/collections';

@Component({
  selector: 'app-user-table',
  templateUrl: './user-table.component.html',
  styleUrls: ['./user-table.component.scss']
})
export class UserTableComponent implements OnInit {

  constructor(private nsrestService: NSRESTService) {}


  dataSource: UserDataSourceService;
  displayedColumns = ['select', 'id', 'name', 'email'];
  selection = new SelectionModel(true, null);

  ngOnInit() {
    this.dataSource = new UserDataSourceService(this.nsrestService);
    this.dataSource.loadUsers();
  }

  /** Whether the number of selected elements matches the total number of rows. */
  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.dataSource.data().length;
    return numSelected === numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.dataSource.data().forEach(row => this.selection.select(row));
  }
}
