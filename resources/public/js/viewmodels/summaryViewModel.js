window.user = window.user || {};

ko.validation.init({
	registerExtenders : true,
	messagesOnModified : true,
	insertMessages : false,
	parseInputAttributes : true,
	messageTemplate : null
}, true);

window.user.summaryViewModel = (function(ko) {

	var activateModalFromTemplate, createSummaryCall, saveTextSummaryCall;
	var higherLevel = this;

	higherLevel.isInitialized = ko.observable(false);

	higherLevel.text1 = ko.observable("");
	higherLevel.text2 = ko.observable("");
	higherLevel.text3 = ko.observable("");
	higherLevel.summary = ko.observable("");
	higherLevel.numberOfThessis = ko.observable(3).extend({
		number : true
	});
	higherLevel.saveTextSummaryVisible = ko.observable(false);
	errors = ko.validation.group([ higherLevel.numberOfThessis ]);
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

	dialogInner = function(title, question, yesCallback, noCallback) {
		var confirmDialogModule = {
			title : ko.observable(title),
			question : ko.observable(question),
			yesCallback : yesCallback,
			noCallback : noCallback,
			canClose : true
		};

		activateModalFromTemplate(confirmDialogModule, "confirmdialogtemplate");
	};

	createSummaryCall = function(text1, text2, text3, numberOfThessis, callback) {
		$.ajax({
			dataType : "json",
			type : "POST",
			url : "/getsummary",
			data : {
				text1 : text1,
				text2 : text2,
				text3 : text3,
				numberOfThessis : numberOfThessis
			},
			success : callback
		});
	};

	higherLevel.createSummary = function() {
		if (errors().length === 0) {
			if (higherLevel.text1().length > 0 && higherLevel.text2().length > 0) {
				createSummaryCall(higherLevel.text1(), higherLevel.text2(), higherLevel.text3(), higherLevel.numberOfThessis().toString(), function(serverData) {
					higherLevel.summary("- " + serverData.resp);
					higherLevel.saveTextSummaryVisible(true);
				});
			} else {
				$.notify("First two paragraphs are required! !", "error");
			}
		} else {
			$.notify("Please fix all errors before proceeding.", "error");
			errors.showAllMessages();
		}
	};

	saveTextSummaryCall = function(text, summary, callback) {
		$.ajax({
			dataType : "json",
			type : "POST",
			url : "/save_text_summary",
			data : {
				text : text,
				summary : summary
			},
			success : callback
		});
	};

	higherLevel.saveTextSummary = function() {
		var yesCallback = function() {
			var callback = function(serverData) {
				if (serverData.Ok === "true") {
					$.notify("Text and summary successfully saved !", "success");
					higherLevel.saveTextSummaryVisible(false);
				} else {
					$.notify("Error while saving text and summary !", "error");
				}

			}
			var text = higherLevel.text1() + " " + higherLevel.text2() + " " +higherLevel.text3();
			saveTextSummaryCall(text, higherLevel.summary(), callback);
		}
		dialogInner(
				"Do you want to save your text and its summary?",
				"You can preview all your saved text and summaries on user page.",
				yesCallback);
	};

	higherLevel.isInitialized(true);

})(ko);

ko.applyBindings(window.user.summaryViewModel);