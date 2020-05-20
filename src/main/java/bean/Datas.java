package bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: Avalon
 * @Date: 20/5/6 15:51
 * @Description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Datas {

  private String year;
  private String url;
  private String ids;
  private String value;
  private String supValue;
  private byte[] json;

}
