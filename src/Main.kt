import java.awt.Rectangle
import java.awt.Robot
import java.awt.Toolkit
import java.awt.image.BufferedImage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.activation.DataHandler
import javax.activation.DataSource
import javax.activation.FileDataSource
import javax.imageio.ImageIO
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

fun main(args: Array<String>) {
    val fromEmail = args[0]
    val fromPassword = args[1]
    val toEmail = args[2]
    val directoryPath = args[3]
    val period = args[4].toInt()
    val properties: Properties = System.getProperties()
    properties.put("mail.smtp.auth", "true")
    properties.put("mail.smtp.starttls.enable", "true")
    properties.put("mail.smtp.host", "smtp.gmail.com")
    properties.put("mail.smtp.port", "587")

    val session = Session.getInstance(properties, object : Authenticator() {
        override fun getPasswordAuthentication(): PasswordAuthentication? =
            PasswordAuthentication(fromEmail, fromPassword)
    })

    while (true) {
        try {
            val image: BufferedImage = Robot().createScreenCapture(Rectangle(Toolkit.getDefaultToolkit().screenSize))
            val filePath = "$directoryPath/recyclableScreenshot.png"
            ImageIO.write(image, "png", File(filePath))
            send(session, toEmail, filePath, getTime())
            Thread.sleep(period * 1000L)
        } catch (mex: MessagingException) {
            println("${getTime()} WAS NOT SENT !!!")
            mex.printStackTrace()
        }
    }
}

fun getTime(): String {
    val sdf = SimpleDateFormat("HH-mm-ss")
    return sdf.format(Date())
}

fun send(session: Session, recipient: String, filePath: String, timeString: String) {
    val message = MimeMessage(session)
    message.addRecipient(Message.RecipientType.TO, InternetAddress(recipient))
    message.subject = timeString

    val source: DataSource = FileDataSource(filePath)
    val messageBodyPart = MimeBodyPart()
    messageBodyPart.dataHandler = DataHandler(source)
    messageBodyPart.fileName = "$timeString.png"

    val multipart: Multipart = MimeMultipart()
    multipart.addBodyPart(messageBodyPart)
    message.setContent(multipart)

    Transport.send(message)
    println("$timeString WAS SENT TO $recipient")
}