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
public class Region {
    Integer stationId;
    String provinceName;
    String stationName;
    Float latitude;
    Float longitude;
    Integer elevation;
}
