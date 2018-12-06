package beam.aws;

import beam.core.BeamCredentials;
import beam.core.BeamResource;
import beam.core.diff.ResourceName;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateRouteResponse;
import software.amazon.awssdk.services.ec2.model.DescribeRouteTablesResponse;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.Route;
import software.amazon.awssdk.services.ec2.model.RouteTable;

import java.util.Set;

@ResourceName("route")
public class RouteResource extends AwsResource {

    private String routeTableId;
    private String destinationCidrBlock;
    private String destinationIpv6CidrBlock;
    private String destinationPrefixListId;
    private String egressOnlyInternetGatewayId;
    private String gatewayId;
    private String instanceId;
    private String instanceOwnerId;
    private String natGatewayId;
    private String networkInterfaceId;
    private String transitGatewayId;
    private String vpcPeeringConnectionId;

    public String getRouteTableId() {
        return routeTableId;
    }

    public void setRouteTableId(String routeTableId) {
        this.routeTableId = routeTableId;
    }

    public String getDestinationCidrBlock() {
        return destinationCidrBlock;
    }

    public void setDestinationCidrBlock(String destinationCidrBlock) {
        this.destinationCidrBlock = destinationCidrBlock;
    }

    public String getDestinationIpv6CidrBlock() {
        return destinationIpv6CidrBlock;
    }

    public void setDestinationIpv6CidrBlock(String destinationIpv6CidrBlock) {
        this.destinationIpv6CidrBlock = destinationIpv6CidrBlock;
    }

    public String getDestinationPrefixListId() {
        return destinationPrefixListId;
    }

    public void setDestinationPrefixListId(String destinationPrefixListId) {
        this.destinationPrefixListId = destinationPrefixListId;
    }

    public String getEgressOnlyInternetGatewayId() {
        return egressOnlyInternetGatewayId;
    }

    public void setEgressOnlyInternetGatewayId(String egressOnlyInternetGatewayId) {
        this.egressOnlyInternetGatewayId = egressOnlyInternetGatewayId;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getInstanceOwnerId() {
        return instanceOwnerId;
    }

    public void setInstanceOwnerId(String instanceOwnerId) {
        this.instanceOwnerId = instanceOwnerId;
    }

    public String getNatGatewayId() {
        return natGatewayId;
    }

    public void setNatGatewayId(String natGatewayId) {
        this.natGatewayId = natGatewayId;
    }

    public String getNetworkInterfaceId() {
        return networkInterfaceId;
    }

    public void setNetworkInterfaceId(String networkInterfaceId) {
        this.networkInterfaceId = networkInterfaceId;
    }

    public String getTransitGatewayId() {
        return transitGatewayId;
    }

    public void setTransitGatewayId(String transitGatewayId) {
        this.transitGatewayId = transitGatewayId;
    }

    public String getVpcPeeringConnectionId() {
        return vpcPeeringConnectionId;
    }

    public void setVpcPeeringConnectionId(String vpcPeeringConnectionId) {
        this.vpcPeeringConnectionId = vpcPeeringConnectionId;
    }

    @Override
    public void refresh(BeamCredentials cloud) {
        Ec2Client client = createClient(Ec2Client.class);

        DescribeRouteTablesResponse response = client.describeRouteTables(r -> r.filters(
                Filter.builder().name("route-table-id").values(getRouteTableId()).build()
        ));

        Route route = null;
        for (RouteTable routeTable : response.routeTables()) {
            for (Route r : routeTable.routes()) {
                if (r.destinationCidrBlock().equals(getDestinationCidrBlock())) {
                    route = r;
                    break;
                } else if (r.destinationIpv6CidrBlock().equals(getDestinationCidrBlock())) {
                    route = r;
                    break;
                } else if (r.destinationPrefixListId().equals(getDestinationPrefixListId())) {
                    route = r;
                    break;
                }
            }

            if (route != null) {
                setEgressOnlyInternetGatewayId(route.egressOnlyInternetGatewayId());
                setGatewayId(route.gatewayId());
                setInstanceId(route.instanceId());
                setInstanceOwnerId(route.instanceOwnerId());
                setNatGatewayId(route.natGatewayId());
                setNetworkInterfaceId(route.networkInterfaceId());
                setTransitGatewayId(route.transitGatewayId());
                setVpcPeeringConnectionId(route.vpcPeeringConnectionId());
            }

            return;
        }
    }

    @Override
    public void create() {
        Ec2Client client = createClient(Ec2Client.class);

        CreateRouteResponse response = client.createRoute(r -> r.destinationCidrBlock(getDestinationCidrBlock())
                .destinationIpv6CidrBlock(getDestinationIpv6CidrBlock())
                .gatewayId(getGatewayId())
                .instanceId(getInstanceId())
                .natGatewayId(getNatGatewayId())
                .networkInterfaceId(getNetworkInterfaceId())
                .transitGatewayId(getTransitGatewayId())
                .vpcPeeringConnectionId(getVpcPeeringConnectionId())
                .routeTableId(getRouteTableId())
        );
    }

    @Override
    public void update(BeamResource current, Set<String> changedProperties) {
        Ec2Client client = createClient(Ec2Client.class);

        client.replaceRoute(r -> r.destinationCidrBlock(getDestinationCidrBlock())
                .destinationIpv6CidrBlock(getDestinationIpv6CidrBlock())
                .gatewayId(getGatewayId())
                .instanceId(getInstanceId())
                .natGatewayId(getNatGatewayId())
                .networkInterfaceId(getNetworkInterfaceId())
                .transitGatewayId(getTransitGatewayId())
                .vpcPeeringConnectionId(getVpcPeeringConnectionId())
                .routeTableId(getRouteTableId())
        );
    }

    @Override
    public void delete() {
        Ec2Client client = createClient(Ec2Client.class);

        client.deleteRoute(r -> r.destinationCidrBlock(getDestinationCidrBlock())
                .destinationIpv6CidrBlock(getDestinationIpv6CidrBlock())
                .routeTableId(getRouteTableId())
        );
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("route ");
        sb.append(getDestinationCidrBlock());

        if (getGatewayId() != null) {
            sb.append(" through gateway ");
            sb.append(getGatewayId());
        } else if (getInstanceId() != null) {
            sb.append(" through instance ");
            sb.append(getInstanceId());
        } else if (getGatewayId() != null) {
            sb.append(" through nat gateway");
            sb.append(getNatGatewayId());
        } else if (getNetworkInterfaceId() != null) {
            sb.append(" through network interface ");
            sb.append(getNetworkInterfaceId());
        } else if (getTransitGatewayId() != null) {
            sb.append(" through transit gateway ");
            sb.append(getTransitGatewayId());
        } else if (getVpcPeeringConnectionId() != null) {
            sb.append(" through vpc peering connection ");
            sb.append(getVpcPeeringConnectionId());
        }

        return sb.toString();
    }

}