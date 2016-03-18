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

package org.talend.dataprep.api.service.command.folder;

import static org.talend.dataprep.command.Defaults.asNull;
import static org.talend.dataprep.exception.error.APIErrorCodes.UNABLE_TO_DELETE_FOLDER;

import java.net.URISyntaxException;

import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.service.APIService;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

@Component
@Scope("request")
public class RenameFolder
    extends GenericCommand<Void> {

    public RenameFolder(String path, String newPath) {
        super(APIService.DATASET_GROUP);
        execute(() -> onExecute(path, newPath));
        onError(e -> new TDPException(UNABLE_TO_DELETE_FOLDER, e, ExceptionContext.build()));
        on(HttpStatus.OK).then(asNull());
    }

    private HttpRequestBase onExecute( String path, String newPath) {
        try {

            URIBuilder uriBuilder = new URIBuilder(datasetServiceUrl + "/folders/rename");
            uriBuilder.addParameter("path", path);
            uriBuilder.addParameter( "newPath", newPath );
            return new HttpPut(uriBuilder.build());
        } catch (URISyntaxException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

}
