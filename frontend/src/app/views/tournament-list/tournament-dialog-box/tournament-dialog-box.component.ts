import {Component, Inject, Optional} from '@angular/core';
import {Tournament} from "../../../models/tournament";
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {TournamentType} from "../../../models/tournament-type";
import {Action} from "../../../action";
import {ScoreType} from "../../../models/score-type";
import {RbacService} from "../../../services/rbac/rbac.service";
import {RbacBasedComponent} from "../../../components/RbacBasedComponent";
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {RbacActivity} from "../../../services/rbac/rbac.activity";
import {TournamentImageSelectorComponent} from "./tournament-image-selector/tournament-image-selector.component";

@Component({
  selector: 'app-tournament-dialog-box',
  templateUrl: './tournament-dialog-box.component.html',
  styleUrls: ['./tournament-dialog-box.component.scss']
})
export class TournamentDialogBoxComponent extends RbacBasedComponent {

  tournament: Tournament;
  title: string;
  action: Action;
  actionName: string;
  tournamentType: TournamentType[];
  scoreTypes: ScoreType[];

  registerForm: FormGroup;

  constructor(
    public dialogRef: MatDialogRef<TournamentDialogBoxComponent>, rbacService: RbacService,
    //@Optional() is used to prevent error if no data is passed
    @Optional() @Inject(MAT_DIALOG_DATA) public data: { title: string, action: Action, entity: Tournament },
    public dialog: MatDialog,) {
    super(rbacService)
    this.tournament = data.entity;
    this.title = data.title;
    this.action = data.action;
    this.actionName = Action[data.action];
    this.tournamentType = TournamentType.toArray();
    this.scoreTypes = ScoreType.toArray();

    this.registerForm = new FormGroup({
      tournamentName: new FormControl({
        value: this.tournament.name,
        disabled: !rbacService.isAllowed(RbacActivity.EDIT_TOURNAMENT)
      }, [Validators.required, Validators.minLength(4), Validators.maxLength(20)]),
      shiaijos: new FormControl({
        value: this.tournament.shiaijos,
        disabled: !rbacService.isAllowed(RbacActivity.EDIT_TOURNAMENT)
      }, [Validators.required, Validators.pattern("^[0-9]*$")]),
      tournamentType: new FormControl({
        value: this.tournament.type,
        disabled: !rbacService.isAllowed(RbacActivity.EDIT_TOURNAMENT)
      }, [Validators.required, Validators.minLength(2), Validators.maxLength(40)]),
      teamSize: new FormControl({
        value: this.tournament.teamSize,
        disabled: !rbacService.isAllowed(RbacActivity.EDIT_TOURNAMENT)
      }, [Validators.required, Validators.pattern("^[0-9]*$")]),
      duelsDuration: new FormControl({
        value: this.tournament.duelsDuration,
        disabled: !rbacService.isAllowed(RbacActivity.EDIT_TOURNAMENT)
      }, [Validators.required, Validators.maxLength(20)]),
      scoreTypes: new FormControl({
        value: this.tournament.tournamentScore?.scoreType,
        disabled: !rbacService.isAllowed(RbacActivity.EDIT_TOURNAMENT)
      }, [Validators.required])
    },);
  }

  doAction() {
    this.tournament.name = this.registerForm.get('tournamentName')!.value;
    this.tournament.shiaijos = this.registerForm.get('shiaijos')!.value;
    this.tournament.type = this.registerForm.get('tournamentType')!.value;
    this.tournament.teamSize = this.registerForm.get('teamSize')!.value;
    this.tournament.duelsDuration = this.registerForm.get('duelsDuration')!.value;
    if (this.tournament.tournamentScore) {
      this.tournament.tournamentScore.scoreType = this.registerForm.get('scoreTypes')!.value;
    }
    this.dialogRef.close({data: this.tournament, action: this.action});
  }

  closeDialog() {
    this.dialogRef.close({action: Action.Cancel});
  }

  getTournamentTypeTranslationTag(tournamentType: TournamentType): string {
    if (!tournamentType) {
      return "";
    }
    return TournamentType.toCamel(tournamentType);
  }

  addPicture() {
    this.openDialog("", Action.Add, this.tournament);
  }

  openDialog(title: string, action: Action, tournament: Tournament) {
    const dialogRef = this.dialog.open(TournamentImageSelectorComponent, {
      width: '700px',
      data: {
        title: title, action: action, tournament: tournament
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result == undefined) {
        //Do nothing
      } else if (result.action == Action.Add) {
        // this.addRowData(result.data);
      } else if (result.action == Action.Update) {
        // this.updateRowData(result.data);
      } else if (result.action == Action.Delete) {
        // this.deleteRowData(result.data);
      }
    });
  }

  deletePicture() {

  }
}
