/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/
import inventoryTile from './inventory-tile-component';

(() => {

    /**
     * @ngdoc object
     * @name data-prep.preparation-tile
     * @description This module contains the preparation tile
     */

    angular.module('data-prep.preparation-tile', [])
        .component('inventoryTile', inventoryTile);
})();