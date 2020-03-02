import {Component, OnInit, ViewChild} from '@angular/core';

import {NSRESTService} from '../ns-rest.service';
import {SelectionModel} from '@angular/cdk/collections';
import {MatTable} from '@angular/material/table';
import {RoleDataSourceService} from '../role-data-source.service';
import {Role} from '../model/Role';

@Component({
  selector: 'app-role-table',
  templateUrl: './role-table.component.html',
  styleUrls: ['./role-table.component.scss']
})
export class RoleTableComponent implements OnInit {


  constructor(private nsrestService: NSRESTService) {
  }

  dataSource: RoleDataSourceService;
  displayedColumns = ['select', 'id', 'key', 'name'];
  selection = new SelectionModel<Role>(false, null);
  clickedRole: Role;
  @ViewChild(MatTable) table: MatTable<Role>;

  ngOnInit() {
    this.selection.isSelected = this.isChecked.bind(this);
    this.dataSource = new RoleDataSourceService(this.nsrestService);
    this.dataSource.loadRoles().subscribe();
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
    this.clickedRole = user;
  }

  onUpdate() {
    const oldSelections = this.selection;
    this.dataSource.loadRoles().subscribe(() => {
      this.table.renderRows();
      oldSelections.selected.forEach(selected => this.selection.select(selected) );
    });
  }

  isChecked(row: any): boolean {
    const found = this.selection.selected.find(el => el.id === row.id);
    return !!found;
  }
}
