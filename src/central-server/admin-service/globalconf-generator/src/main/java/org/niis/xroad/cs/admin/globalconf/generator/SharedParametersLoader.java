/*
 * The MIT License
 *
 * Copyright (c) 2019- Nordic Institute for Interoperability Solutions (NIIS)
 * Copyright (c) 2018 Estonian Information System Authority (RIA),
 * Nordic Institute for Interoperability Solutions (NIIS), Population Register Centre (VRK)
 * Copyright (c) 2015-2017 Estonian Information System Authority (RIA), Population Register Centre (VRK)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.niis.xroad.cs.admin.globalconf.generator;

import ee.ria.xroad.common.identifier.ClientId;
import ee.ria.xroad.common.util.CryptoUtils;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.niis.xroad.cs.admin.api.domain.AuthCert;
import org.niis.xroad.cs.admin.api.domain.FlattenedSecurityServerClientView;
import org.niis.xroad.cs.admin.api.domain.GlobalGroup;
import org.niis.xroad.cs.admin.api.domain.GlobalGroupMember;
import org.niis.xroad.cs.admin.api.domain.SecurityServer;
import org.niis.xroad.cs.admin.api.dto.CertificateAuthority;
import org.niis.xroad.cs.admin.api.dto.CertificationService;
import org.niis.xroad.cs.admin.api.dto.OcspResponder;
import org.niis.xroad.cs.admin.api.service.CentralServicesService;
import org.niis.xroad.cs.admin.api.service.CertificationServicesService;
import org.niis.xroad.cs.admin.api.service.ClientService;
import org.niis.xroad.cs.admin.api.service.GlobalGroupMemberService;
import org.niis.xroad.cs.admin.api.service.GlobalGroupService;
import org.niis.xroad.cs.admin.api.service.MemberClassService;
import org.niis.xroad.cs.admin.api.service.SecurityServerService;
import org.niis.xroad.cs.admin.api.service.SystemParameterService;
import org.niis.xroad.cs.admin.api.service.TimestampingServicesService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Component
@RequiredArgsConstructor
@Slf4j
class SharedParametersLoader {
    private final SystemParameterService systemParameterService;
    private final CertificationServicesService certificationServicesService;
    private final TimestampingServicesService timestampingServicesService;
    private final ClientService clientService;
    private final SecurityServerService securityServerService;
    private final GlobalGroupService globalGroupService;
    private final GlobalGroupMemberService globalGroupMemberService;
    private final CentralServicesService centralServicesService;
    private final MemberClassService memberClassService;


    SharedParameters load() {
        var parameters = new SharedParameters();
        parameters.setInstanceIdentifier(systemParameterService.getInstanceIdentifier());
        parameters.setApprovedCAs(getApprovedCAs());
        parameters.setApprovedTSAs(getApprovedTSAs());
        parameters.setMembers(getMembers());
        parameters.setSecurityServers(getSecurityServers());
        parameters.setGlobalGroups(getGlobalGroups());
        parameters.setCentralServices(getCentralServices());
        parameters.setGlobalSettings(getGlobalSettings());
        return parameters;
    }

    private List<SharedParameters.ApprovedCA> getApprovedCAs() {
        var approvedCas = certificationServicesService.findAll();
        return approvedCas.stream()
                .map(this::toApprovedCa)
                .collect(toList());
    }

    private SharedParameters.ApprovedCA toApprovedCa(CertificationService ca) {
        var approvedCA = new SharedParameters.ApprovedCA();
        approvedCA.setName(ca.getName());
        approvedCA.setAuthenticationOnly(ca.getTlsAuth());
        approvedCA.setCertificateProfileInfo(ca.getCertificateProfileInfo());
        approvedCA.setTopCA(new SharedParameters.CaInfo(toOcspInfos(ca.getOcspResponders()), ca.getCertificate()));
        approvedCA.setIntermediateCAs(toCaInfos(ca.getIntermediateCas()));
        return approvedCA;
    }

    private List<SharedParameters.CaInfo> toCaInfos(List<CertificateAuthority> cas) {
        return cas.stream()
                .map(ca -> new SharedParameters.CaInfo(toOcspInfos(ca.getOcspResponders()), ca.getCaCertificate().getEncoded()))
                .collect(toList());
    }

    private List<SharedParameters.OcspInfo> toOcspInfos(List<OcspResponder> ocspResponders) {
        return ocspResponders.stream()
                .map(this::toOcspInfo)
                .collect(toList());
    }

    private SharedParameters.OcspInfo toOcspInfo(OcspResponder ocsp) {
        return new SharedParameters.OcspInfo(ocsp.getUrl(), ocsp.getCertificate());
    }

    private List<SharedParameters.ApprovedTSA> getApprovedTSAs() {
        return timestampingServicesService.getTimestampingServices().stream()
                .map(tsa -> new SharedParameters.ApprovedTSA(tsa.getName(), tsa.getUrl(), tsa.getCertificate().getEncoded()))
                .collect(toList());
    }

    private List<SharedParameters.Member> getMembers() {
        return new MemberMapper().map(clientService.findAll());

    }

    private List<SharedParameters.SecurityServer> getSecurityServers() {
        return securityServerService.findAll().stream()
                .map(this::toSecurityServer)
                .collect(toList());
    }

    private SharedParameters.SecurityServer toSecurityServer(SecurityServer ss) {
        var result = new SharedParameters.SecurityServer();
        result.setOwner(ss.getOwner().getIdentifier());
        result.setAddress(ss.getAddress());
        result.setServerCode(ss.getServerCode());
        result.setClients(getSecurityServerClients(ss.getId()));
        result.setAuthCertHashes(ss.getAuthCerts().stream()
                .map(AuthCert::getCert)
                .map(SharedParametersLoader::certHash)
                .collect(toList()));
        return result;
    }

    private List<ClientId> getSecurityServerClients(int id) {
        return clientService.find(new ClientService.SearchParameters().setSecurityServerId(id))
                .stream().map(SharedParametersLoader::toClientId).collect(toList());

    }

    @SneakyThrows
    private static byte[] certHash(byte[] cert) {
        return CryptoUtils.certHash(cert);
    }

    private static ClientId toClientId(FlattenedSecurityServerClientView client) {
        return ClientId.Conf.create(client.getXroadInstance(),
                client.getMemberClass().getCode(),
                client.getMemberCode(),
                client.getSubsystemCode());
    }

    private List<SharedParameters.GlobalGroup> getGlobalGroups() {
        return globalGroupService.findGlobalGroups().stream()
                .map(this::getGlobalGroup)
                .collect(toList());
    }

    private SharedParameters.GlobalGroup getGlobalGroup(GlobalGroup globalGroup) {
        return new SharedParameters.GlobalGroup(
                globalGroup.getGroupCode(),
                globalGroup.getDescription(),
                getGroupMembers(globalGroup.getId()));
    }

    private List<ClientId> getGroupMembers(int id) {
        return globalGroupMemberService.findByGroupId(id).stream()
                .map(GlobalGroupMember::getIdentifier)
                .collect(toList());
    }

    private List<SharedParameters.CentralService> getCentralServices() {
        return centralServicesService.findAll().stream()
                .map(centralService ->
                        new SharedParameters.CentralService(centralService.getServiceCode(), centralService.getIdentifier()))
                .collect(toList());
    }

    private SharedParameters.GlobalSettings getGlobalSettings() {
        var memberClasses = memberClassService.findAll().stream()
                .map(memberClass -> new SharedParameters.MemberClass(memberClass.getCode(), memberClass.getDescription()))
                .collect(toList());

        return new SharedParameters.GlobalSettings(memberClasses, systemParameterService.getOcspFreshnessSeconds());
    }

    static class MemberMapper {
        private Map<ClientId, List<SharedParameters.Subsystem>> subsystems;

        List<SharedParameters.Member> map(List<FlattenedSecurityServerClientView> flattenedClients) {
            subsystems = new HashMap<>();
            var members = new ArrayList<SharedParameters.Member>();
            for (FlattenedSecurityServerClientView client: flattenedClients) {
                if (client.getSubsystemCode() == null) {
                    members.add(toMember(client));
                } else {
                    addSubSystem(client);
                }
            }
            return members;
        }

        private void addSubSystem(FlattenedSecurityServerClientView client) {
            var clientId = toClientId(client);
            getSubsystemList(toMemberId(clientId)).add(new SharedParameters.Subsystem(client.getSubsystemCode(), clientId));
        }

        private SharedParameters.Member toMember(FlattenedSecurityServerClientView client) {
            var clientId = toClientId(client);
            var member = new SharedParameters.Member();
            member.setId(clientId);
            member.setMemberClass(
                    new SharedParameters.MemberClass(client.getMemberClass().getCode(), client.getMemberClass().getDescription()));
            member.setMemberCode(client.getMemberCode());
            member.setName(client.getMemberName());
            member.setSubsystems(getSubsystemList(clientId));
            return member;
        }

        private List<SharedParameters.Subsystem> getSubsystemList(ClientId clientId) {
            return subsystems.computeIfAbsent(clientId, clId -> new ArrayList<>());
        }

        private ClientId toMemberId(ClientId clientId) {
            return ClientId.Conf.create(clientId.getXRoadInstance(), clientId.getMemberClass(), clientId.getMemberCode());
        }
    }
}
