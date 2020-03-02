import {Component, ElementRef, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {User} from '../model/User';
import {NSRESTService} from '../ns-rest.service';
import {RoleDataSourceService} from '../role-data-source.service';
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
  selector: 'app-user-detail',
  templateUrl: './user-detail.component.html',
  styleUrls: ['./user-detail.component.scss']
})
export class UserDetailComponent implements OnInit {

  @Input() user: User;
  private roleSource: RoleDataSourceService;
  removable = true;
  selectable = true;
  readonly separatorKeysCodes: number[] = [ENTER, COMMA, TAB];
  addOnBlur = true;
  private permissionSource: PermissionDataSourceService;
  @ViewChild('roleInput') roleInput: ElementRef<HTMLInputElement>;
  @ViewChild('auto') matAutocomplete: MatAutocomplete;
  roleCtrl = new FormControl();
  filteredRoles: Observable<Role[]>;
  private allRoles: Role[] = [];
  @Output() updated = new EventEmitter<User>();

  constructor(private REST: NSRESTService) {
    this.filteredRoles  = this.roleCtrl.valueChanges.pipe(
      startWith(null),
      map((roleTypedStr: string | null) => roleTypedStr ? this._filter(roleTypedStr) : this.allRoles.slice()));
  }

  ngOnInit(): void {
    this.roleSource = new RoleDataSourceService(this.REST);
    this.roleSource.loadRoles().toPromise().then((roleSource) => this.allRoles = roleSource);

    this.permissionSource = new PermissionDataSourceService(this.REST);
    this.permissionSource.loadPermissions();
  }


  removePerm(perm: Permission) {
    const index = this.user.intrinsicPermissions.indexOf(perm);

    if (index >= 0) {
      this.user.intrinsicPermissions.splice(index, 1);
      this.updateUser();
    }
  }

  removeRole(role: Role) {
    const index = this.user.roles.indexOf(role);

    if (index >= 0) {
      this.user.roles.splice(index, 1);
      this.updateUser();
    }
  }

  private updateUser() {
    this.REST.update('AppUser', this.user).subscribe(() => {
      this.updated.emit(this.user);
      this.REST.get('AppUser', this.user.id).subscribe( (u) => this.user = u[0]);
      this.roleSource.loadRoles().toPromise().then((roleSource) => this.allRoles = roleSource);
    });
  }

  addRole(event: MatChipInputEvent) {
    const input = event.input;
    const value = event.value;

    // Add our role
    if (!this.user.roles) {
      this.user.roles = [];
    }
    if ((value || '').trim()) {
      const role = this._filter(value.trim())[0];
      role.members.push(this.user);
      this.user.roles.push(role);
    }

    // Reset the input value
    if (input) {
      input.value = '';
    }

    this.roleCtrl.setValue(null);
    this.updateUser();
  }


  selectedRole(event: MatAutocompleteSelectedEvent) {
    if (!this.user.roles) {
      this.user.roles = [];
    }
    this.user.roles.push(this._filter(event.option.viewValue)[0]);
    this.updateUser();
    this.roleInput.nativeElement.value = '';
    this.roleCtrl.setValue(null);
  }

  private _filter(value: string): Role[] {
    const filterValue = value.toLowerCase();
    return this.allRoles.filter(role => role.name.toLowerCase().indexOf(filterValue) === 0);
  }
}
