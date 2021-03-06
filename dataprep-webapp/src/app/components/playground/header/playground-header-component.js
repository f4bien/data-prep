/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

/**
 * @ngdoc directive
 * @name talend.sunchoke.accordion.directive:ScAccordionAnimation
 * @description Accordion animation on open/close
 * @restrict A
 * @usage
 <playground-header  dataset="dataset"
                     display-nb-lines="displayNbLines"
                     preview="previewInProgress"
                     lookup="lookupInProgress"
                     on-parameters="toggleParameters()"
                     on-lookup="toggleLookup()"
                     on-onboarding="startOnBoarding(')"
                     on-feedback="openFeedback()"
                     on-close="close()"></playground-header>
 * @param {object} dataset The current dataset
 * @param {boolean} displayNbLines Whether or not the number of lines should be displayed
 * @param {boolean} preview A preview is in progress
 * @param {boolean} lookup A lookup is in progress
 * @param {function} onParameters Callback on gear icon click
 * @param {function} onLookup Callback on lookup icon click
 * @param {function} onOnboarding Callback on onboarding icon click
 * @param {function} onFeedback Callback on feedback icon click
 * @param {function} onClose Callback on close icon click
 */
const PlaygroundHeader = {
    templateUrl: 'app/components/playground/header/playground-header.html',
    bindings: {
        dataset: '<',
        displayNbLines: '<',
        preview: '<',
        lookup: '<',
        onParameters: '&',
        onLookup: '&',
        onOnboarding: '&',
        onFeedback: '&',
        onClose: '&',
        onOpenPreparationPicker: '&',
        activePreparationPickerBtn: '='
    }
};

export default PlaygroundHeader;