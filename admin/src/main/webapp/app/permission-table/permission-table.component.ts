import {Component, OnInit, ViewChild} from '@angular/core';

import {NSRESTService} from '../ns-rest.service';
import {SelectionModel} from '@angular/cdk/collections';
import {MatTable} from '@angular/material/table';
import {PermissionDataSourceService} from '../permission-data-source.service';
import {Permission} from '../model/Permission';

@Component({
  selector: 'app-permission-table',
  templateUrl: './permission-table.component.html',
  styleUrls: ['./permission-table.component.scss']
})
export class PermissionTableComponent implements OnInit {

  constructor(private nsrestService: NSRESTService) {
  }

  dataSource: PermissionDataSourceService;
  displayedColumns = ['select', 'id', 'action', 'type', 'objId', 'field'];
  selection = new SelectionModel<Permission>(false, null);
  clickedPermission: Permission;
  @ViewChild(MatTable) table: MatTable<Permission>;

  ngOnInit() {
    this.selection.isSelected = this.isChecked.bind(this);
    this.dataSource = new PermissionDataSourceService(this.nsrestService);
    this.dataSource.loadPermissions().subscribe();
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
    this.clickedPermission = user;
  }

  onUpdate() {
    const oldSelections = this.selection;
    this.dataSource.loadPermissions().subscribe(() => {
      this.table.renderRows();
      oldSelections.selected.forEach(selected => this.selection.select(selected) );
    });
  }

  isChecked(row: any): boolean {
    const found = this.selection.selected.find(el => el.id === row.id);
    return !!found;
  }
}
