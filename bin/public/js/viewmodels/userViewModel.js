window.user = window.user || {};

ko.validation.init({
	registerExtenders : true,
	messagesOnModified : true,
	insertMessages : false,
	parseInputAttributes : true,
	messageTemplate : null
}, true);

window.user.userViewModel = (function(ko) {

	var initData, errors, SummaryCreator, getUserDataCall, editProfileCall, editProfileWithNewPwdCall, getSummariesCall;
	var higherLevel = this;

	higherLevel.username = ko.observable("").extend({
		required : true
	});
	higherLevel.firstName = ko.observable("").extend({
		required : true
	});
	higherLevel.lastName = ko.observable("").extend({
		required : true
	});
	higherLevel.email = ko.observable("").extend({
		required : true,
		email : true
	});
	higherLevel.wantNewPassword = ko.observable(false);
	higherLevel.newPwdRetype = ko.observable("");
	higherLevel.newpwd = ko
			.observable("")
			.extend(
					{
						validation : {
							validator : function(val) {
								return val === higherLevel.newPwdRetype();
							},
							message : 'Passwords do not match !'
						},
						validation : {
							validator : function(val) {
								return val.length > 0
										|| higherLevel.wantNewPassword() === false;
							},
							message : 'New password is required if you want to change password !'
						}
					});

	higherLevel.summaries = ko.observableArray();

	SummaryCreator = function(serverItem) {

		var levelUp = this;

		levelUp.summary = serverItem.summary;
		levelUp.text = serverItem.text;
	};

	createSummaries = function(serverData) {
		var newArray = [];
		$.each(serverData, function(i) {
			newArray.push(new SummaryCreator(this, i));
		});

		higherLevel.summaries(newArray);
	};

	editProfileWithNewPwdCall = function(username, firstName, lastName, email,
			newpwd, callback) {
		$.ajax({
			dataType : "json",
			type : "POST",
			url : "/edit_profile_new_pwd",
			data : {
				usr : username,
				first_name : firstName,
				last_name : lastName,
				email : email,
				newpwd : newpwd
			},
			success : callback
		});
	};

	getUserDataCall = function(callback) {
		$.getJSON("/user_profile", callback);
	};

	editProfileCall = function(username, firstName, lastName, email, callback) {
		$.ajax({
			dataType : "json",
			type : "POST",
			url : "/edit_profile",
			data : {
				usr : username,
				first_name : firstName,
				last_name : lastName,
				email : email
			},
			success : callback
		});
	};

	higherLevel.editUser = function() {
		if (errors().length === 0) {
			if (higherLevel.wantNewPassword()) {
				editProfileWithNewPwdCall(higherLevel.username(), higherLevel
						.firstName(), higherLevel.lastName(), higherLevel
						.email(), higherLevel.newpwd(), function(serverData) {
					if (serverData.Ok === "true") {
						getUserDataCall(initData);
						higherLevel.wantNewPassword(false);
						$.notify("Profile successfully updated !", "success");
					} else {
						$.notify("Error while updating profile !", "error");
					}
				});
			} else {

				editProfileCall(higherLevel.username(),
						higherLevel.firstName(), higherLevel.lastName(),
						higherLevel.email(), function(serverData) {
							if (serverData.Ok === "true") {
								getUserDataCall(initData);
								$.notify("Profile successfully updated !",
										"success");
							} else {
								$.notify("Error while updating profile !",
										"error");
							}
						});
			}
		} else {
			$.notify("Please fix all errors before proceeding.", "error");
			errors.showAllMessages();
		}
	};

	getSummariesCall = function(callback) {
		$.getJSON("/get_text_summary", callback);
	};

	higherLevel.showMySummaries = function() {
		getSummariesCall(createSummaries);
	};

	initData = function(serverData) {
		higherLevel.username(serverData.username);
		higherLevel.firstName(serverData.first_name);
		higherLevel.lastName(serverData.last_name);
		higherLevel.email(serverData.email);
		higherLevel.newPwdRetype("");
		higherLevel.newpwd("");
		errors = ko.validation.group([ higherLevel.username,
				higherLevel.firstName, higherLevel.lastName, higherLevel.email,
				higherLevel.newpwd ]);
	};

	getUserDataCall(initData);

})(ko);

ko.applyBindings(window.user.userViewModel);