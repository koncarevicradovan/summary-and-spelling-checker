window.admin = window.admin || {};

ko.validation.init({
	registerExtenders : true,
	messagesOnModified : true,
	insertMessages : false,
	parseInputAttributes : true,
	messageTemplate : null
}, true);

window.admin.administratorViewModel = (function(ko) {

	var initData, UserAdministration, activateModalFromTemplate, getUserCall, User, UserPopupModel, changeUserCall, getInitDataCall, deleteUserCall, makeUserCall;
	var higherLevel = this;

	getInitDataCall = function(callback) {
		$.getJSON("/users", callback);
	};

	makeUserCall = function(username, firstName, lastName, email, password,
			isAdmin, callback) {
		$.ajax({
			dataType : "json",
			type : "POST",
			url : "/create_user",
			data : {
				usr : username,
				first_name : firstName,
				last_name : lastName,
				email : email,
				pwd : password,
				isAdmin : isAdmin
			},
			success : callback
		});
	};

	UserAdministration = function(serverItem) {

		var levelUp = this;

		levelUp.firstName = serverItem.first_name;
		levelUp.lastName = serverItem.last_name;
		levelUp.email = serverItem.email;
		levelUp.username = serverItem.username;
	};

	User = function() {

		var levelUp = this;

		levelUp.username = ko.observable("").extend({
			required : true
		});
		levelUp.firstName = ko.observable("").extend({
			required : true
		});
		levelUp.lastName = ko.observable("").extend({
			required : true
		});
		levelUp.email = ko.observable("").extend({
			required : true,
			email : true
		});
		levelUp.password = ko.observable("").extend({
			minLength : {
				message : "Password must contains at least 6 characters",
				params : 6
			}
		});
		levelUp.confirmPassword = ko.observable("").extend({
			validation : {
				validator : function(val) {
					return val === levelUp.password();
				},
				message : 'Passwords do not match !'
			},
		});
		levelUp.isAdmin = ko.observable(false);
	};

	initData = function(serverData) {
		var newArray = [];
		$.each(serverData, function(i) {
			newArray.push(new UserAdministration(this, i));
		});

		higherLevel.users(newArray);
	};

	activateModalFromTemplate = function(module, templateName) {
		var $el, $modal;
		$el = $("<div>");
		$('body').append($el);
		ko.renderTemplate(templateName, module, null, $el.get(0));
		$modal = $el.children(":first");

		$modal.on('hidden.bs.modal', function() {
			if (module.close) {
				module.close();
			}

			$el.remove();
		});

		$modal.on('shown.bs.modal', function() {
			$('input:text:visible:first', this).focus();
		});

		module.closeModal = function() {
			$modal.modal('hide');
		};

		if (module.canClose || module.canClose === undefined) {
			$modal.modal();
		} else {
			$modal.modal({
				keyboard : false,
				backdrop : 'static'
			});
		}
	};

	getUserCall = function(usr, callback) {
		$.getJSON("/user", {
			usr : usr
		}, callback);
	};

	changeUserCall = function(username, firstName, lastName, email, callback) {
		$.ajax({
			dataType : "json",
			type : "POST",
			url : "/user",
			data : {
				usr : username,
				first_name : firstName,
				last_name : lastName,
				email : email
			},
			success : callback
		});
	};

	deleteUserCall = function(username, callback) {
		$.ajax({
			dataType : "json",
			type : "DELETE",
			url : "/user",
			data : {
				usr : username
			},
			success : callback
		});
	};

	UserPopupModel = function(usr) {
		var higherLevel = this;
		higherLevel.firstName = ko.observable("");
		higherLevel.lastName = ko.observable("");
		higherLevel.email = ko.observable("");
		higherLevel.username = ko.observable("");

		higherLevel.editUser = function() {
			changeUserCall(this.username, this.firstName, this.lastName,
					this.email, function(serverData) {
						if (serverData.Ok === "true") {
							getInitDataCall(initData);
							$.notify("User successfully updated !", "success");
						} else {
							$.notify("Error while updating user !", "error");
						}
					});
		};

		higherLevel.removeUser = function() {
			deleteUserCall(this.username, function(serverData) {
				if (serverData.Ok === "true") {
					higherLevel.closeModal();
					getInitDataCall(initData);
					$.notify("User successfully deleted !", "success");
				} else {
					$.notify("Error while deleting user !", "error");
				}
			});
		};
		getUserCall(usr, function(serverData) {
			higherLevel.firstName(serverData.first_name);
			higherLevel.lastName(serverData.last_name);
			higherLevel.email(serverData.email);
			higherLevel.username(serverData.username);
		});

	};

	UserAdministration.prototype.openUserDetails = function() {
		var popupModule = new UserPopupModel(this.username);
		popupModule.canClose = true;
		activateModalFromTemplate(popupModule, "UserDialogTemplate");
	};

	higherLevel.createNewUSer = function() {
		higherLevel.isCreating(true);
	};

	higherLevel.showUsers = function() {
		higherLevel.isCreating(false);
	};

	User.prototype.createUser = function() {
		if (higherLevel.errors().length === 0) {
			makeUserCall(higherLevel.userViewModel().username, higherLevel
					.userViewModel().firstName,
					higherLevel.userViewModel().lastName, higherLevel
							.userViewModel().email,
					higherLevel.userViewModel().password, higherLevel
							.userViewModel().isAdmin, function(serverData) {
						if (serverData.Ok === "true") {
							higherLevel.userViewModel(new User());
							getInitDataCall(initData);
							higherLevel.isCreating(false);
							$.notify("User successfully created !", "success");
						} else {
							$.notify("Error while creating user !", "error");
						}
					});
		} else {
			$.notify("Please fix all errors before proceeding.", "error");
			higherLevel.errors.showAllMessages();
		}
	};

	higherLevel.users = ko.observableArray();
	higherLevel.userViewModel = ko.observable(new User());
	higherLevel.isCreating = ko.observable(false);

	higherLevel.errors = ko.validation.group([
			higherLevel.userViewModel().username,
			higherLevel.userViewModel().confirmPassword,
			higherLevel.userViewModel().firstName,
			higherLevel.userViewModel().email,
			higherLevel.userViewModel().lastName,
			higherLevel.userViewModel().password ]);

	getInitDataCall(initData);

})(ko);

ko.applyBindings(window.admin.administratorViewModel);