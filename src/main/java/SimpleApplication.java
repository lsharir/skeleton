import controllers.*;
import dao.ReceiptDao;
import dao.TagDao;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.server.session.SessionHandler;
import org.h2.jdbcx.JdbcConnectionPool;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;

public class SimpleApplication extends Application<Configuration> {
    public static void main(String[] args) throws Exception {
        new SimpleApplication().run(args);
    }

    private static void enableSessionSupport(Environment env) {
        env.servlets().setSessionHandler(new SessionHandler());
    }

    public static org.jooq.Configuration setupJooq() {
        // For now we are just going to use an H2 Database.  We'll upgrade to mysql later
        // This connection string tells H2 to initialize itself with our schema.sql before allowing connections
        final String jdbcUrl = "jdbc:h2:mem:test;MODE=MySQL;INIT=RUNSCRIPT from 'classpath:schema.sql'";
        JdbcConnectionPool cp = JdbcConnectionPool.create(jdbcUrl, "sa", "sa");

        // This sets up jooq to talk to whatever database we are using.
        org.jooq.Configuration jooqConfig = new DefaultConfiguration();
        jooqConfig.set(SQLDialect.MYSQL);   // Lets stick to using MySQL (H2 is OK with this!)
        jooqConfig.set(cp);
        return jooqConfig;
    }

    @Override
    public void run(Configuration cfg, Environment env) {
        // Create any global resources you need here
        org.jooq.Configuration jooqConfig = setupJooq();
        ReceiptDao receiptDao = new ReceiptDao(jooqConfig);
        TagDao tagDao = new TagDao(jooqConfig);

        // Register all Controllers below.  Don't forget
        // you need class and method @Path annotations!
        env.jersey().register(new StaticHtmlController());
        env.jersey().register(new ReceiptController(receiptDao));
        env.jersey().register(new TagController(tagDao, receiptDao));
        env.jersey().register(new ReceiptImageController());
    }
}
