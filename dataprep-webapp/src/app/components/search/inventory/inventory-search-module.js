/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import InventorySearch from './inventory-search-component';

(() => {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.data-prep.inventory-search
     * @description This module contains the component to manage an inventory search
     * @requires talend.widget
     * @requires data-prep.services.documentation
     * @requires data-prep.services.inventory
     * @requires data-prep.services.utils
     * @requires data-prep.services.easter-eggs
     */
    angular.module('data-prep.inventory-search',
        [
            'pascalprecht.translate',
            'data-prep.search-bar',
            'data-prep.services.documentation',
            'data-prep.services.inventory',
            'data-prep.services.utils',
            'data-prep.services.easter-eggs'
        ])
        .component('inventorySearch', InventorySearch);
})();

