//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.api.service;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;

@RestController
@Scope("request")
public class UserServiceAPI extends APIService {


    /**
     * Returns all the versions of the different services (api, dataset, preparation and transformation).
     *
     * @return an array of service versions
     */
    @RequestMapping(value = "/api/user/info", method = GET)
    @ApiOperation(value = "Return the user information", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getUserInfo () {
        return "{}";
    }

}