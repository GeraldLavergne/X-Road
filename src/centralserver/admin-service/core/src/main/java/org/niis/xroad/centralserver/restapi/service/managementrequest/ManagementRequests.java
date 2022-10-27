/**
 * The MIT License
 * <p>
 * Copyright (c) 2019- Nordic Institute for Interoperability Solutions (NIIS)
 * Copyright (c) 2018 Estonian Information System Authority (RIA),
 * Nordic Institute for Interoperability Solutions (NIIS), Population Register Centre (VRK)
 * Copyright (c) 2015-2017 Estonian Information System Authority (RIA), Population Register Centre (VRK)
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.niis.xroad.centralserver.restapi.service.managementrequest;

import org.niis.xroad.centralserver.restapi.dto.AuthenticationCertificateDeletionRequestDto;
import org.niis.xroad.centralserver.restapi.dto.AuthenticationCertificateRegistrationRequestDto;
import org.niis.xroad.centralserver.restapi.dto.ClientDeletionRequestDto;
import org.niis.xroad.centralserver.restapi.dto.ClientRegistrationRequestDto;
import org.niis.xroad.centralserver.restapi.dto.ManagementRequestDto;
import org.niis.xroad.cs.admin.api.domain.Request;
import org.niis.xroad.cs.admin.api.dto.ManagementRequestInfoDto;
import org.niis.xroad.cs.admin.core.entity.AuthenticationCertificateDeletionRequestEntity;
import org.niis.xroad.cs.admin.core.entity.AuthenticationCertificateRegistrationRequestEntity;
import org.niis.xroad.cs.admin.core.entity.ClientDeletionRequestEntity;
import org.niis.xroad.cs.admin.core.entity.ClientRegistrationRequestEntity;
import org.niis.xroad.cs.admin.core.entity.ManagementRequestViewEntity;
import org.niis.xroad.cs.admin.core.entity.RequestEntity;

final class ManagementRequests {

    private ManagementRequests() {
        //Utility class
    }

    static ManagementRequestDto asDto(RequestEntity request) {
        if (request instanceof AuthenticationCertificateRegistrationRequestEntity) {
            var req = (AuthenticationCertificateRegistrationRequestEntity) request;
            return new AuthenticationCertificateRegistrationRequestDto(
                    req.getId(),
                    req.getOrigin(),
                    req.getSecurityServerId(),
                    req.getProcessingStatus(),
                    req.getAuthCert(),
                    req.getAddress());
        }

        if (request instanceof AuthenticationCertificateDeletionRequestEntity) {
            var req = (AuthenticationCertificateDeletionRequestEntity) request;
            return new AuthenticationCertificateDeletionRequestDto(
                    req.getId(),
                    req.getOrigin(),
                    req.getSecurityServerId(),
                    req.getProcessingStatus(),
                    req.getAuthCert());
        }

        if (request instanceof ClientRegistrationRequestEntity) {
            var req = (ClientRegistrationRequestEntity) request;
            return new ClientRegistrationRequestDto(
                    req.getId(),
                    req.getOrigin(),
                    req.getSecurityServerId(),
                    req.getProcessingStatus(),
                    req.getClientId());
        }

        if (request instanceof ClientDeletionRequestEntity) {
            var req = (ClientDeletionRequestEntity) request;
            return new ClientDeletionRequestDto(
                    req.getId(),
                    req.getOrigin(),
                    req.getSecurityServerId(),
                    req.getProcessingStatus(),
                    req.getClientId());
        }

        throw new IllegalArgumentException("Unknown request type");
    }

    static ManagementRequestInfoDto asInfoDto(Request req) {
        return new ManagementRequestInfoDto(
                req.getId(),
                req.getManagementRequestType(),
                req.getOrigin(),
                null, //TODO should be a separate DTO. This field is not needed in this UC
                req.getSecurityServerId(),
                req.getProcessingStatus(),
                req.getCreatedAt());
    }

    static ManagementRequestInfoDto asInfoDto(final ManagementRequestViewEntity managementRequestView) {
        return new ManagementRequestInfoDto(
                managementRequestView.getId(),
                managementRequestView.getManagementRequestType(),
                managementRequestView.getOrigin(),
                managementRequestView.getSecurityServerOwnerName(),
                managementRequestView.getSecurityServerId(),
                managementRequestView.getRequestProcessingStatus(),
                managementRequestView.getCreatedAt());
    }
}
