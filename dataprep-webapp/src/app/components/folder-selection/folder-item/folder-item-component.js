/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

const FolderItem = {
    bindings: {
        item: '=',
        onToggle: '&',
        onSelect: '&'
    },
    template: `<li style="margin-left:{{$ctrl.item.level * 20}}px"
            ng-class="{'folder-selected': $ctrl.item.selected}"
            ng-dblclick="$ctrl.onToggle({folder: $ctrl.item})" >
            <a class="inventory-icon inventory-folder">
                <span ng-if="!$ctrl.item.hasNoChildren" ng-switch="!!$ctrl.item.collapsed" ng-click="$ctrl.onToggle({folder: $ctrl.item})">
                    <i data-icon="J" ng-switch-when="false" class="dropdown-button-right"></i>
                    <i data-icon="I" ng-switch-when="true" class="dropdown-button-down"></i>
                </span>
                <i ng-if="$ctrl.item.hasNoChildren" class="empty-caret"></i>

                <span ng-switch="!!$ctrl.item.collapsed" ng-click="$ctrl.onSelect({dest:$ctrl.item})">
                    <img class="icon-img" ng-switch-when="false" src="assets/images/folder/folder_open_small-icon.png"/>
                    <img class="icon-img" ng-switch-when="true" src="assets/images/folder/folder_close_small-icon.png"/>
                    <span id="{{$ctrl.item.path}}"
                        class="folder-name">
                        {{$ctrl.item.name}}
                    </span>
                </span>
            </a>
        </li>`
};


export default FolderItem;