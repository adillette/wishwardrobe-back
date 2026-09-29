package today.wishwordrobe.configuration;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus (HttpStatus.SERVICE_UNAVAILABLE) 
public class WeatherUnavailableException extends RuntimeException{
    public WeatherUnavailableException(String message) { super(message); }
}
