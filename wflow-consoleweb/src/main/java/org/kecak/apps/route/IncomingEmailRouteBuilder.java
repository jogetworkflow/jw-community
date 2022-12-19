package org.kecak.apps.route;

import org.apache.camel.builder.RouteBuilder;
import org.joget.commons.util.SecurityUtil;
import org.kecak.apps.incomingEmail.dao.IncomingEmailDao;
import org.kecak.apps.incomingEmail.model.IncomingEmail;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

public class IncomingEmailRouteBuilder extends RouteBuilder {
    private String incomingEmailId;
    private IncomingEmailDao incomingEmailDao;

    @Override
    public void configure() throws Exception {
        final Optional<IncomingEmail> optIncomingEmail = Optional.ofNullable(incomingEmailId).map(incomingEmailDao::load);

        if(optIncomingEmail.isPresent()) {
            // load single
            optIncomingEmail.ifPresent(this::configure);
        } else {
            // load all
            Optional.ofNullable(incomingEmailDao.find(null, null, null, null, null, null))
                    .map(Collection::stream)
                    .orElseGet(Stream::empty)
                    .filter(IncomingEmail::getActive)
                    .forEach(this::configure);
        }
    }

    protected void configure(IncomingEmail incomingEmail) {
        @Nonnull String emailAccount = incomingEmail.getUsername();
        @Nonnull String emailPassword = SecurityUtil.decrypt(incomingEmail.getPassword());
        @Nonnull String emailProtocol = incomingEmail.getProtocol();
        @Nonnull String emailHost = incomingEmail.getHost();
        @Nonnull int emailPort = incomingEmail.getPort();
        @Nonnull String emailFolder = incomingEmail.getFolder();

        // set default folder
        StringBuilder fromUriBuilder = new StringBuilder()
                .append(emailProtocol).append("://").append(emailHost).append(":").append(emailPort)
                .append("?username=").append(emailAccount)
                .append("&password=").append(emailPassword)
                .append("&folderName=").append(emailFolder)
                .append("&delete=false&unseen=true");

        String fromUri = fromUriBuilder.toString();
        from(fromUri).beanRef("emailProcessor", "parseEmail");
    }

    public void setIncomingEmailDao(IncomingEmailDao incomingEmailDao) {
        this.incomingEmailDao = incomingEmailDao;
    }

    public void setIncomingEmailId(String incomingEmailId) {
        this.incomingEmailId = incomingEmailId;
    }
}
