import {Component, ElementRef, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';

import {NSRESTService} from '../ns-rest.service';
import {Permission} from '../model/Permission';
import {Role} from '../model/Role';
import {COMMA, ENTER, TAB} from '@angular/cdk/keycodes';
import {MatChipInputEvent} from '@angular/material/chips';
import {PermissionDataSourceService} from '../permission-data-source.service';
import {MatAutocomplete, MatAutocompleteSelectedEvent} from '@angular/material/autocomplete';
import {FormControl} from '@angular/forms';
import {Observable} from 'rxjs';
import {map, startWith} from 'rxjs/operators';


@Component({
  selector: 'app-role-detail',
  templateUrl: './role-detail.component.html',
  styleUrls: ['./role-detail.component.scss']
})
export class RoleDetailComponent implements OnInit {


  constructor(private REST: NSRESTService) {
    this.filteredPermissions = this.permissionCtrl.valueChanges.pipe(
      startWith(null),
      map((permStr: string | null) => permStr ? this._filterPerms(permStr) : this.allPermissions.slice()));
  }

  @Input() role: Role;
  removable = true;
  selectable = true;
  readonly separatorKeysCodes: number[] = [ENTER, COMMA, TAB];
  addOnBlur = true;

  private permissionSource: PermissionDataSourceService;

  @ViewChild('permissionInput') permissionInput: ElementRef<HTMLInputElement>;
  @ViewChild('autoPerms') matAutocompletePerms: MatAutocomplete;
  permissionCtrl = new FormControl();
  filteredPermissions: Observable<Permission[]>;
  private allPermissions: Permission[] = [];
  @Output() updated = new EventEmitter<Role>();

  permString(perm: Permission): string {
    return perm.action + ':' + perm.type + ':' + perm.objId + ':' + perm.field;
  }

  ngOnInit(): void {

    this.permissionSource = new PermissionDataSourceService(this.REST);
    this.permissionSource.loadPermissions().toPromise().then((perms) => this.allPermissions = perms);
  }

  removePerm(perm: Permission) {
    const index = this.role.grants.indexOf(perm);

    if (index >= 0) {
      this.role.grants.splice(index, 1);
      this.updateRole();
    }
  }

  private updateRole() {
    this.REST.update('Role', this.role).subscribe(() => {
      this.updated.emit(this.role);
      this.REST.get('Role', this.role.id).subscribe((u) => this.role = u[0]);
    });
  }

  addPermission(event: MatChipInputEvent) {
    const input = event.input;
    const value = event.value;

    // Add our role
    if (!this.role.grants) {
      this.role.grants = [];
    }
    if ((value || '').trim()) {
      const perm = this._filterPerms(value.trim())[0];
      this.role.grants.push(perm);
    }

    // Reset the input value
    if (input) {
      input.value = '';
    }

    this.permissionCtrl.setValue(null);
    this.updateRole();
  }


  selectedPermission(event: MatAutocompleteSelectedEvent) {
    if (!this.role.grants) {
      this.role.grants = [];
    }
    this.role.grants.push(this._filterPerms(event.option.viewValue)[0]);
    this.updateRole();
    this.permissionInput.nativeElement.value = '';
    this.permissionCtrl.setValue(null);
  }

  private _filterPerms(value: string): Permission[] {
    const filterValue = value.toLowerCase();
    return this.allPermissions.filter(perm => this.permString(perm).toLowerCase().indexOf(filterValue) >= 0);
  }
}
