/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import FolderSelectionCtrl from './folder-selection-controller';

const FolderSelection = {
    bindings: {
        selectedFolder: '=ngModel'
    },
    bindToController: true,
    controllerAs: 'folderSelectionCtrl',
    controller: FolderSelectionCtrl,
    template: `
    <div class="folders-modal">
        <input type="search"
               class="action-search"
               id="action-suggestions-search"
               translate-once-placeholder="FIND_FOLDER"
               ng-model="folderSelectionCtrl.searchFolderQuery"
               ng-model-options="{debounce: { default: 300, blur: 0 }}"
               ng-change="folderSelectionCtrl.populateSearchResult()"
               talend-search-input
        >
        <ul class="folders">
            <folder-item
                    ng-repeat="item in folderSelectionCtrl.folderItems track by item.path"
                    item="item"
                    ng-if="item.display"
                    on-toggle="folderSelectionCtrl.toggle(folder)"
                    on-select="folderSelectionCtrl.chooseFolder(dest)"
            ></folder-item>
        </ul>
    </div>`
};

export default FolderSelection;