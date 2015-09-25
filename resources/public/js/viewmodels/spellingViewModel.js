window.user = window.user || {};

window.user.spellingViewModel = (function(ko) {

	var activateModalFromTemplate, correctTextCall, correctWordCall, trainCorrectorCall;
	var higherLevel = this;

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

	correctWordCall = function(word, callback) {
		$.ajax({
			dataType : "json",
			type : "POST",
			url : "/correctword",
			data : {
				word : word
			},
			success : callback
		});
	};

	higherLevel.correctWord = function() {
		if (higherLevel.word().length > 0) {
			correctWordCall(higherLevel.word(), function(serverData) {
				higherLevel.correctedWord(serverData.resp);
				higherLevel.enableTrainWord(true);
				higherLevel.enableTrainNewWord(true);
			});
		} else {
			$.notify("Word field can not be empty !", "error");
		}
	};

	correctTextCall = function(text, callback) {
		$.ajax({
			dataType : "json",
			type : "POST",
			url : "/correcttext",
			data : {
				text : text
			},
			success : callback
		});
	};

	higherLevel.correctText = function() {
		if (higherLevel.text().length > 0) {
			correctTextCall(higherLevel.text(), function(serverData) {
				higherLevel.correctedText(serverData.resp);
				higherLevel.enableTrainText(true)
			});
		} else {
			$.notify("Text field can not be empty !", "error");
		}
	};

	trainCorrectorCall = function(word, callback) {
		$.ajax({
			dataType : "json",
			type : "POST",
			url : "/traincorrector",
			data : {
				word : word
			},
			success : callback
		});
	};

	higherLevel.trainWithWord = function() {
		var yesCallback = function() {
			var callback = function(serverData) {
				if (serverData.Ok === "true") {
					$.notify("Your dataset is trained successfully !",
							"success");
					higherLevel.saveTextSummaryVisible(false);
				} else {
					$.notify("Error while training your dataset !", "error");
				}

			}
			trainCorrectorCall(higherLevel.correctedWord(), callback);
		}
		dialogInner(
				"If you are satisfied with corrected word you can train your dataset?",
				"This will improve your corrector.", yesCallback);
	};

	higherLevel.trainWithNewWord = function() {
		var yesCallback = function() {
			var callback = function(serverData) {
				if (serverData.Ok === "true") {
					$.notify("Your dataset is trained successfully !",
							"success");
					higherLevel.saveTextSummaryVisible(false);
				} else {
					$.notify("Error while training your dataset !", "error");
				}

			}
			trainCorrectorCall(higherLevel.newword(), callback);
		}
		if (higherLevel.newword().length > 0) {
			dialogInner(
					"If you are satisfied with corrected word you can train your dataset?",
					"This will improve your corrector.", yesCallback);
		} else {
			$.notify("New word field can not be empty !", "error");
		}
	};

	higherLevel.trainWithText = function() {
		var yesCallback = function() {
			var callback = function(serverData) {
				if (serverData.Ok === "true") {
					$.notify("Your dataset is trained successfully !",
							"success");
					higherLevel.saveTextSummaryVisible(false);
				} else {
					$.notify("Error while training your dataset !", "error");
				}
			}
			trainCorrectorCall(higherLevel.correctedText(), callback);
		}
		dialogInner(
				"If you are very very satisfied with corrected text you can train your dataset?",
				"This will improve your corrector.", yesCallback);
	};

	higherLevel.text = ko.observable("");
	higherLevel.correctedText = ko.observable("");
	higherLevel.word = ko.observable("");
	higherLevel.newword = ko.observable("");
	higherLevel.correctedWord = ko.observable("");
	higherLevel.enableTrainWord = ko.observable(false);
	higherLevel.enableTrainText = ko.observable(false);
	higherLevel.enableTrainNewWord = ko.observable(false);

})(ko);

ko.applyBindings(window.user.spellingViewModel);