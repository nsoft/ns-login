import {Component, OnInit, ViewChild} from '@angular/core';
import {UserDataSourceService} from './user-data-source.service';
import {NSRESTService} from '../ns-rest.service';
import {SelectionModel} from '@angular/cdk/collections';
import {User} from '../model/User';
import {MatTable} from '@angular/material/table';

@Component({
  selector: 'app-user-table',
  templateUrl: './user-table.component.html',
  styleUrls: ['./user-table.component.scss']
})
export class UserTableComponent implements OnInit {

  constructor(private nsrestService: NSRESTService) {
  }

  dataSource: UserDataSourceService;
  displayedColumns = ['select', 'id', 'name', 'email'];
  selection = new SelectionModel<User>(false, null);
  clickedUser: User;
  @ViewChild(MatTable) table: MatTable<User>;

  ngOnInit() {
    this.selection.isSelected = this.isChecked.bind(this);
    this.dataSource = new UserDataSourceService(this.nsrestService);
    this.dataSource.loadUsers().subscribe();
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

  rowClicked(event, user) {
    event.stopPropagation();
    this.clickedUser = user;
  }

  onUpdate() {
    const oldSelections = this.selection;
    this.dataSource.loadUsers().subscribe(() => {
      this.table.renderRows();
      oldSelections.selected.forEach(selected => this.selection.select(selected) );
    });
  }

  isChecked(row: any): boolean {
    const found = this.selection.selected.find(el => el.id === row.id);
    return !!found;
  }
}
