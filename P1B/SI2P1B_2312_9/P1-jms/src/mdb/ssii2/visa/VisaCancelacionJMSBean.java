/**
 * Pr&aacute;ctricas de Sistemas Inform&aacute;ticos II
 * VisaCancelacionJMSBean.java
 */

package ssii2.visa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.ejb.ActivationConfigProperty;
import javax.jms.MessageListener;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.JMSException;
import javax.annotation.Resource;
import java.util.logging.Logger;

/**
 * @author jaime
 */
@MessageDriven(mappedName = "jms/VisaPagosQueue")
public class VisaCancelacionJMSBean extends DBTester implements MessageListener {
  static final Logger logger = Logger.getLogger("VisaCancelacionJMSBean");
  @Resource
  private MessageDrivenContext mdc;

  private static final String UPDATE_CODRESPUESTA_QRY = "update pago " +
                                                        "set codRespuesta=999 " +
                                                        "where idAutorizacion=?";

  private static final String UPDATE_SALDO_QRY = "update tarjeta " +
                                                  "set saldo = saldo + pago.importe "  +
                                                  "from pago " +
                                                  "where pago.idAutorizacion=? " +
                                                  "and pago.numeroTarjeta = tarjeta.numeroTarjeta";

  public VisaCancelacionJMSBean() {
  }

  // TODO : Método onMessage de ejemplo
  // Modificarlo para ejecutar el UPDATE definido más arriba,
  // asignando el idAutorizacion a lo recibido por el mensaje
  // Para ello conecte a la BD, prepareStatement() y ejecute correctamente
  // la actualización
  public void onMessage(Message inMessage) {
      TextMessage msg = null;
      Connection con = null;
      PreparedStatement pstmt = null;
      try {
          if (inMessage instanceof TextMessage) {
            msg = (TextMessage) inMessage;
            logger.info("MESSAGE BEAN: Message received: " + msg.getText());
            int idAutorizacion = Integer.parseInt(msg.getText());
            con = getConnection();

            pstmt = con.prepareStatement(UPDATE_CODRESPUESTA_QRY);
            pstmt.setInt(1,idAutorizacion);
            logger.info("UPDATE COD RESPUESTA: " + UPDATE_CODRESPUESTA_QRY);
            pstmt.executeQuery();

            pstmt = con.prepareStatement(UPDATE_SALDO_QRY);
            pstmt.setInt(1,idAutorizacion);
            logger.info("UPDATE SALDO: " + UPDATE_SALDO_QRY);
            pstmt.executeQuery();

          } else {
              logger.warning(
                      "Message of wrong type: "
                      + inMessage.getClass().getName());
          }
      } catch (JMSException e) {
          e.printStackTrace();
          mdc.setRollbackOnly();
      } catch (Throwable te) {
          te.printStackTrace();
      }
  }
}
